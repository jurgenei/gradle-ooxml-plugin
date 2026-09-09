package name.jurgenei.gradle.ooxml;

import jakarta.xml.bind.JAXBException;
import name.jurgenei.gradle.ooxml.canonical.CanonicalDocument;
import name.jurgenei.xml.sexpr.SExpressionSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Serializes canonical model objects to canonical S-expression syntax.
 */
final class CanonicalSexprSerializer {
    private final CanonicalXmlSerializer xmlSerializer = new CanonicalXmlSerializer();

    void write(CanonicalDocument document, Path outputFile) throws IOException {
        Files.createDirectories(outputFile.getParent());
        try (OutputStream outputStream = Files.newOutputStream(outputFile)) {
            write(document, outputStream);
        }
    }

    void write(CanonicalDocument document, OutputStream outputStream) throws IOException {
        final String xml;
        try {
            xml = xmlSerializer.toXml(document);
        } catch (JAXBException e) {
            throw new IOException("Failed to serialize canonical XML", e);
        }
        final SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        } catch (Exception ignored) {
            // Best effort hardening across parser implementations.
        }

        try {
            final XMLReader reader = factory.newSAXParser().getXMLReader();
            final Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
            final SExpressionSerializer serializer = new SExpressionSerializer(
                    writer,
                    SExpressionSerializer.OutputFormat.BEAUTIFIED,
                    SExpressionSerializer.SyntaxMode.CANONICAL);
            reader.setContentHandler(serializer);
            reader.setProperty("http://xml.org/sax/properties/lexical-handler", serializer);
            reader.parse(new InputSource(new StringReader(xml)));
        } catch (Exception e) {
            throw new IOException("Failed to serialize canonical S-expression", e);
        }
    }
}

