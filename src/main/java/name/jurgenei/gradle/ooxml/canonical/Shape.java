package name.jurgenei.gradle.ooxml.canonical;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import name.jurgenei.gradle.ooxml.CanonicalNamespace;

/**
 * Canonical diagram shape node.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Shape {
    @XmlAttribute(name = "id")
    private String id;

    @XmlElement(name = "Label", namespace = CanonicalNamespace.URI)
    private String label;

    public Shape() {
    }

    /**
     * Creates a shape node.
     *
     * @param id source-derived shape id.
     * @param label optional shape label.
     */
    public Shape(String id, String label) {
        this.id = id;
        this.label = label;
    }

    /**
     * @return source-derived shape id.
     */
    public String getId() {
        return id;
    }

    /**
     * @return optional shape label.
     */
    public String getLabel() {
        return label;
    }
}

