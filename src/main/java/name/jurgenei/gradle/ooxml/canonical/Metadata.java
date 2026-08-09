package name.jurgenei.gradle.ooxml.canonical;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import name.jurgenei.gradle.ooxml.CanonicalNamespace;

/**
 * Canonical metadata captured from source filename/type context.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Metadata {
    @XmlElement(name = "DocumentId", namespace = CanonicalNamespace.URI, required = true)
    private String documentId;

    @XmlElement(name = "Version", namespace = CanonicalNamespace.URI)
    private String version;

    @XmlElement(name = "SourceFile", namespace = CanonicalNamespace.URI, required = true)
    private String sourceFile;

    @XmlElement(name = "DocumentType", namespace = CanonicalNamespace.URI, required = true)
    private String documentType;

    public Metadata() {
    }

    public Metadata(String documentId, String version, String sourceFile, String documentType) {
        this.documentId = documentId;
        this.version = version;
        this.sourceFile = sourceFile;
        this.documentType = documentType;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getVersion() {
        return version;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public String getDocumentType() {
        return documentType;
    }
}

