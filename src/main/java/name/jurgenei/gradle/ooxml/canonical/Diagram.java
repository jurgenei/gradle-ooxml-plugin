package name.jurgenei.gradle.ooxml.canonical;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import name.jurgenei.gradle.ooxml.CanonicalNamespace;

import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class Diagram {
    @XmlElement(name = "Shape", namespace = CanonicalNamespace.URI)
    private List<Shape> shapes = new ArrayList<>();

    @XmlElement(name = "Connector", namespace = CanonicalNamespace.URI)
    private List<Connector> connectors = new ArrayList<>();

    public Diagram() {
    }

    public Diagram(List<Shape> shapes, List<Connector> connectors) {
        this.shapes = shapes;
        this.connectors = connectors;
    }

    public List<Shape> getShapes() {
        return shapes;
    }

    public List<Connector> getConnectors() {
        return connectors;
    }
}

