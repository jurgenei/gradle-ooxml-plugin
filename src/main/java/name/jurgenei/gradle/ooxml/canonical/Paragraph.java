package name.jurgenei.gradle.ooxml.canonical;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAnyElement;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import name.jurgenei.gradle.ooxml.CanonicalNamespace;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Canonical paragraph with source provenance attributes.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "para", namespace = CanonicalNamespace.URI)
public class Paragraph {
    @XmlElement(name = "text", namespace = CanonicalNamespace.URI)
    private List<String> texts = new ArrayList<>();

    @XmlAnyElement
    private List<Element> math = new ArrayList<>();

    @XmlAttribute(name = "source-path")
    private String sourcePath;

    @XmlAttribute(name = "label")
    private String label;

    public Paragraph() {
    }

    public Paragraph(String text) {
        addText(text);
    }

    public Paragraph(String text, String sourcePath) {
        addText(text);
        this.sourcePath = sourcePath;
    }

    public Paragraph(String text, String sourcePath, String label) {
        addText(text);
        this.sourcePath = sourcePath;
        this.label = label;
    }

    public String getText() {
        if (texts.isEmpty()) {
            return "";
        }
        return String.join(" ", texts).trim();
    }

    public List<Element> getMath() {
        return math;
    }

    public void addText(String text) {
        if (text != null && !text.isBlank()) {
            texts.add(text.trim());
        }
    }

    public void addMath(Element mathElement) {
        if (mathElement != null) {
            math.add(mathElement);
        }
    }

    public boolean hasContent() {
        return !texts.isEmpty() || !math.isEmpty();
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public String getLabel() {
        return label;
    }
}

