package name.jurgenei.gradle.ooxml;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import name.jurgenei.gradle.ooxml.canonical.CanonicalDocument;
import org.glassfish.jaxb.runtime.v2.JAXBContextFactory;

import java.io.IOException;
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
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(document, outputFile.toFile());
        } catch (JAXBException e) {
            throw new IOException("Failed to serialize canonical XML", e);
        }
    }
}
