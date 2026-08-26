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
@XmlRootElement(name = "graph", namespace = CanonicalNamespace.GRAPHML_URI)
public class Diagram {
    @XmlAttribute(name = "source-path")
    private String sourcePath;

    @XmlAttribute(name = "href")
    private String href;

    @XmlElement(name = "shape", namespace = CanonicalNamespace.GRAPHML_URI)
    private List<Shape> shapes = new ArrayList<>();

    @XmlElement(name = "connector", namespace = CanonicalNamespace.GRAPHML_URI)
    private List<Connector> connectors = new ArrayList<>();

    @XmlElement(name = "node", namespace = CanonicalNamespace.GRAPHML_URI)
    private List<DiagramNode> nodes = new ArrayList<>();

    @XmlElement(name = "edge", namespace = CanonicalNamespace.GRAPHML_URI)
    private List<DiagramEdge> edges = new ArrayList<>();

    @XmlElement(name = "group", namespace = CanonicalNamespace.GRAPHML_URI)
    private List<DiagramGroup> groups = new ArrayList<>();

    @XmlElement(name = "annotation", namespace = CanonicalNamespace.GRAPHML_URI)
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

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
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

