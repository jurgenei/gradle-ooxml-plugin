package name.jurgenei.gradle.ooxml.canonical;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import name.jurgenei.gradle.ooxml.CanonicalNamespace;

@XmlAccessorType(XmlAccessType.FIELD)
public class Shape {
    @XmlAttribute(name = "id")
    private String id;

    @XmlElement(name = "Label", namespace = CanonicalNamespace.URI)
    private String label;

    public Shape() {
    }

    public Shape(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }
}

