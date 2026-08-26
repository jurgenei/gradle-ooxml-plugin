package name.jurgenei.gradle.ooxml;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import name.jurgenei.gradle.ooxml.canonical.CanonicalDocument;
import org.glassfish.jaxb.runtime.v2.JAXBContextFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Serializes canonical model objects to XML using JAXB.
 */
final class CanonicalXmlSerializer {
    private final JAXBContext jaxbContext;

    CanonicalXmlSerializer() {
        try {
            this.jaxbContext = new JAXBContextFactory().createContext(new Class[]{CanonicalDocument.class}, Map.of());
        } catch (JAXBException e) {
            throw new IllegalStateException("Cannot initialize JAXB context: " + e.getMessage(), e);
        }
    }

    /**
     * Writes canonical XML to disk, creating parent directories when necessary.
     *
     * @param document canonical model instance.
     * @param outputFile target output path.
     * @throws IOException if serialization fails.
     */
    void write(CanonicalDocument document, Path outputFile) throws IOException {
        try {
            Files.createDirectories(outputFile.getParent());
            try (OutputStream outputStream = Files.newOutputStream(outputFile)) {
                write(document, outputStream);
            }
        } catch (JAXBException e) {
            throw new IOException("Failed to serialize canonical XML", e);
        }
    }

    void write(CanonicalDocument document, OutputStream outputStream) throws IOException, JAXBException {
        try {
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            marshaller.marshal(document, buffer);

            Document dom = parseXml(buffer.toByteArray());
            normalizeGraphNamespaceStyle(dom);
            writeXml(dom, outputStream);
        } catch (Exception e) {
            if (e instanceof JAXBException jaxbException) {
                throw jaxbException;
            }
            throw new IOException("Failed to serialize canonical XML", e);
        }
    }

    private Document parseXml(byte[] bytes) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        return factory.newDocumentBuilder().parse(new ByteArrayInputStream(bytes));
    }

    private void normalizeGraphNamespaceStyle(Document dom) {
        Element root = dom.getDocumentElement();
        if (root == null) {
            return;
        }

        stripGraphPrefix(root);
        removeGraphNamespacePrefixDeclaration(root);
    }

    private void stripGraphPrefix(Node node) {
        if (node instanceof Element element) {
            if (CanonicalNamespace.GRAPHML_URI.equals(element.getNamespaceURI())) {
                element.setPrefix(null);
            }
            NamedNodeMap attributes = element.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++) {
                Node attr = attributes.item(i);
                if (CanonicalNamespace.GRAPHML_URI.equals(attr.getNamespaceURI())) {
                    attr.setPrefix(null);
                }
            }
        }

        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            stripGraphPrefix(children.item(i));
        }
    }

    private void removeGraphNamespacePrefixDeclaration(Element root) {
        NamedNodeMap attributes = root.getAttributes();
        for (int i = attributes.getLength() - 1; i >= 0; i--) {
            Node attribute = attributes.item(i);
            boolean isXmlns = XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(attribute.getNamespaceURI());
            boolean isGraphNamespace = CanonicalNamespace.GRAPHML_URI.equals(attribute.getNodeValue());
            if (isXmlns && isGraphNamespace) {
                root.removeAttributeNode((org.w3c.dom.Attr) attribute);
            }
        }
    }

    private void writeXml(Document dom, OutputStream outputStream) throws Exception {
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(dom), new StreamResult(buffer));

        String xml = buffer.toString(StandardCharsets.UTF_8);
        xml = xml.replaceAll("\\sxmlns:[A-Za-z0-9_\\-]+=\"http://graphml\\.graphdrawing\\.org/xmlns\"", "");
        outputStream.write(xml.getBytes(StandardCharsets.UTF_8));
    }
}
