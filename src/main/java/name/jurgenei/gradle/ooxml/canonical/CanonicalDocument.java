package name.jurgenei.gradle.ooxml.canonical;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import name.jurgenei.gradle.ooxml.CanonicalNamespace;

/**
 * Root JAXB type for canonical XML output.
 */
@XmlRootElement(name = "Document", namespace = CanonicalNamespace.URI)
@XmlAccessorType(XmlAccessType.FIELD)
public class CanonicalDocument {
    @XmlElement(name = "Metadata", namespace = CanonicalNamespace.URI, required = true)
    private Metadata metadata;

    @XmlElement(name = "Body", namespace = CanonicalNamespace.URI, required = true)
    private Body body;

    public CanonicalDocument() {
    }

    public CanonicalDocument(Metadata metadata, Body body) {
        this.metadata = metadata;
        this.body = body;
    }

    /**
     * @return document-level metadata.
     */
    public Metadata getMetadata() {
        return metadata;
    }

    /**
     * @return canonical content body.
     */
    public Body getBody() {
        return body;
    }
}

