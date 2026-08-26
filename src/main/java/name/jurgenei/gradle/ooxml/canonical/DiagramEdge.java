package name.jurgenei.gradle.ooxml.canonical;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import name.jurgenei.gradle.ooxml.CanonicalNamespace;

/**
 * Semantic, format-neutral diagram edge.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class DiagramEdge {
    @XmlAttribute(name = "source")
    private String source;

    @XmlAttribute(name = "target")
    private String target;

    @XmlAttribute(name = "directed")
    private Boolean directed;

    @XmlAttribute(name = "semantic")
    private String semantic;

    @XmlAttribute(name = "confidence")
    private Double confidence;

    @XmlElement(name = "label", namespace = CanonicalNamespace.GRAPHML_URI)
    private String label;

    public DiagramEdge() {
    }

    public DiagramEdge(String source, String target, Boolean directed, String semantic, Double confidence, String label) {
        this.source = source;
        this.target = target;
        this.directed = directed;
        this.semantic = semantic;
        this.confidence = confidence;
        this.label = label;
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public Boolean getDirected() {
        return directed;
    }

    public String getSemantic() {
        return semantic;
    }

    public Double getConfidence() {
        return confidence;
    }

    public String getLabel() {
        return label;
    }
}

