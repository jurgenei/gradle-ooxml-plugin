package name.jurgenei.gradle.ooxml.canonical;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import name.jurgenei.gradle.ooxml.CanonicalNamespace;

/**
 * Canonical internal/document-local reference.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "reference", namespace = CanonicalNamespace.URI)
public class Reference {
    @XmlAttribute(name = "target")
    private String target;

    @XmlElement(name = "text", namespace = CanonicalNamespace.URI)
    private String text;

    public Reference() {
    }

    /**
     * Creates a canonical reference.
     *
     * @param target reference target identifier.
     * @param text optional reference label.
     */
    public Reference(String target, String text) {
        this.target = target;
        this.text = text;
    }

    /**
     * @return reference target identifier.
     */
    public String getTarget() {
        return target;
    }

    /**
     * @return optional reference text/label.
     */
    public String getText() {
        return text;
    }
}

