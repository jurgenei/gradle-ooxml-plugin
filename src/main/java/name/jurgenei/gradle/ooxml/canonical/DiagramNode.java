package name.jurgenei.gradle.ooxml.canonical;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import name.jurgenei.gradle.ooxml.CanonicalNamespace;

/**
 * Semantic, format-neutral diagram node.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class DiagramNode {
    @XmlAttribute(name = "id")
    private String id;

    @XmlAttribute(name = "geometry")
    private String geometry;

    @XmlAttribute(name = "semantic")
    private String semantic;

    @XmlAttribute(name = "confidence")
    private Double confidence;

    @XmlElement(name = "label", namespace = CanonicalNamespace.GRAPHML_URI)
    private String label;

    public DiagramNode() {
    }

    public DiagramNode(String id, String label, String geometry, String semantic, Double confidence) {
        this.id = id;
        this.label = label;
        this.geometry = geometry;
        this.semantic = semantic;
        this.confidence = confidence;
    }

    public String getId() {
        return id;
    }

    public String getGeometry() {
        return geometry;
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

    public void setSemantic(String semantic) {
        this.semantic = semantic;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }
}

