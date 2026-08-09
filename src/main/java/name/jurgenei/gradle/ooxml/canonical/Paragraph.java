package name.jurgenei.gradle.ooxml.canonical;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import name.jurgenei.gradle.ooxml.CanonicalNamespace;

/**
 * Canonical paragraph with source provenance attributes.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Paragraph {
    @XmlElement(name = "Text", namespace = CanonicalNamespace.URI, required = true)
    private String text;

    @XmlAttribute(name = "source-document")
    private String sourceDocument;

    @XmlAttribute(name = "source-path")
    private String sourcePath;

    public Paragraph() {
    }

    public Paragraph(String text) {
        this.text = text;
    }

    public Paragraph(String text, String sourceDocument, String sourcePath) {
        this.text = text;
        this.sourceDocument = sourceDocument;
        this.sourcePath = sourcePath;
    }

    public String getText() {
        return text;
    }

    public String getSourceDocument() {
        return sourceDocument;
    }

    public String getSourcePath() {
        return sourcePath;
    }
}

