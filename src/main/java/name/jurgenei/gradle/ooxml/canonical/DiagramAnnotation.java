package name.jurgenei.gradle.ooxml.canonical;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import name.jurgenei.gradle.ooxml.CanonicalNamespace;

/**
 * Additional semantic evidence attached to a canonical diagram.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class DiagramAnnotation {
    @XmlAttribute(name = "kind")
    private String kind;

    @XmlAttribute(name = "target")
    private String target;

    @XmlAttribute(name = "confidence")
    private Double confidence;

    @XmlElement(name = "text", namespace = CanonicalNamespace.GRAPHML_URI)
    private String text;

    public DiagramAnnotation() {
    }

    public DiagramAnnotation(String kind, String target, Double confidence, String text) {
        this.kind = kind;
        this.target = target;
        this.confidence = confidence;
        this.text = text;
    }

    public String getKind() {
        return kind;
    }

    public String getTarget() {
        return target;
    }

    public Double getConfidence() {
        return confidence;
    }

    public String getText() {
        return text;
    }
}

