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

    @XmlElement(name = "Node", namespace = CanonicalNamespace.URI)
    private List<DiagramNode> nodes = new ArrayList<>();

    @XmlElement(name = "Edge", namespace = CanonicalNamespace.URI)
    private List<DiagramEdge> edges = new ArrayList<>();

    @XmlElement(name = "Group", namespace = CanonicalNamespace.URI)
    private List<DiagramGroup> groups = new ArrayList<>();

    @XmlElement(name = "Annotation", namespace = CanonicalNamespace.URI)
    private List<DiagramAnnotation> annotations = new ArrayList<>();

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

    public Diagram(List<Shape> shapes,
                   List<Connector> connectors,
                   List<DiagramNode> nodes,
                   List<DiagramEdge> edges,
                   List<DiagramAnnotation> annotations) {
        this(shapes, connectors, nodes, edges, List.of(), annotations);
    }

    public Diagram(List<Shape> shapes,
                   List<Connector> connectors,
                   List<DiagramNode> nodes,
                   List<DiagramEdge> edges,
                   List<DiagramGroup> groups,
                   List<DiagramAnnotation> annotations) {
        this.shapes = shapes;
        this.connectors = connectors;
        this.nodes = nodes;
        this.edges = edges;
        this.groups = groups;
        this.annotations = annotations;
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

    public List<DiagramNode> getNodes() {
        return nodes;
    }

    public List<DiagramEdge> getEdges() {
        return edges;
    }

    public List<DiagramGroup> getGroups() {
        return groups;
    }

    public List<DiagramAnnotation> getAnnotations() {
        return annotations;
    }
}

