package name.jurgenei.gradle.ooxml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;

/**
 * Transforms OMML fragments into MathML fragments using bundled XSLT.
 */
final class OmmlMathTransformer {
    static final String MATHML_NS = "http://www.w3.org/1998/Math/MathML";

    private final Templates templates;

    OmmlMathTransformer() {
        this.templates = loadTemplates();
    }

    Element transform(Element ommlNode) throws IOException {
        try {
            Transformer transformer = templates.newTransformer();
            Document output = newDocument();
            DOMResult result = new DOMResult(output);
            transformer.transform(new DOMSource(ommlNode), result);
            Element root = output.getDocumentElement();
            if (root == null) {
                throw new IOException("OMML to MathML transformation returned an empty document");
            }
            return root;
        } catch (Exception e) {
            throw new IOException("Failed to transform OMML to MathML", e);
        }
    }

    private Templates loadTemplates() {
        try (InputStream input = getClass().getResourceAsStream("/xsl/omml2mathml.xsl")) {
            if (input == null) {
                throw new IllegalStateException("Missing stylesheet resource: /xsl/omml2mathml.xsl");
            }
            TransformerFactory factory = TransformerFactory.newInstance();
            try {
                factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            } catch (Exception ignored) {
                // Some TransformerFactory implementations do not expose this feature.
            }
            return factory.newTemplates(new StreamSource(input));
        } catch (Exception e) {
            throw new IllegalStateException("Cannot load OMML-to-MathML stylesheet", e);
        }
    }

    private Document newDocument() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        return factory.newDocumentBuilder().newDocument();
    }
}

