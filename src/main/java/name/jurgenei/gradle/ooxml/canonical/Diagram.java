package name.jurgenei.gradle.ooxml.canonical;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import name.jurgenei.gradle.ooxml.CanonicalNamespace;

import java.util.ArrayList;
import java.util.List;

/**
 * Canonical diagram topology container.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Diagram", namespace = CanonicalNamespace.URI)
public class Diagram {
    @XmlAttribute(name = "source-path")
    private String sourcePath;

    @XmlElement(name = "Shape", namespace = CanonicalNamespace.URI)
    private List<Shape> shapes = new ArrayList<>();

    @XmlElement(name = "Connector", namespace = CanonicalNamespace.URI)
    private List<Connector> connectors = new ArrayList<>();

    public Diagram() {
    }

    /**
     * Creates a diagram with explicit shape and connector topology.
     *
     * @param shapes diagram nodes.
     * @param connectors diagram edges.
     */
    public Diagram(List<Shape> shapes, List<Connector> connectors) {
        this.shapes = shapes;
        this.connectors = connectors;
    }

    /**
     * @return source provenance path.
     */
    public String getSourcePath() {
        return sourcePath;
    }

    /**
     * Sets source provenance path after extraction.
     *
     * @param sourcePath source provenance path.
     */
    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    /**
     * @return diagram shape nodes.
     */
    public List<Shape> getShapes() {
        return shapes;
    }

    /**
     * @return connector relationships between shapes.
     */
    public List<Connector> getConnectors() {
        return connectors;
    }
}

