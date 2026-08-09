package name.jurgenei.gradle.ooxml.canonical;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import name.jurgenei.gradle.ooxml.CanonicalNamespace;

/**
 * Canonical external or relationship-based link.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Link {
    @XmlAttribute(name = "target")
    private String target;

    @XmlElement(name = "Text", namespace = CanonicalNamespace.URI)
    private String text;

    public Link() {
    }

    /**
     * Creates a canonical link.
     *
     * @param target resolved link target.
     * @param text optional link label.
     */
    public Link(String target, String text) {
        this.target = target;
        this.text = text;
    }

    /**
     * @return resolved link target.
     */
    public String getTarget() {
        return target;
    }

    /**
     * @return optional link text/label.
     */
    public String getText() {
        return text;
    }
}

