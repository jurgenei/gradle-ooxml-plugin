package name.jurgenei.gradle.ooxml.canonical;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import name.jurgenei.gradle.ooxml.CanonicalNamespace;

@XmlAccessorType(XmlAccessType.FIELD)
public class Reference {
    @XmlAttribute(name = "target")
    private String target;

    @XmlElement(name = "Text", namespace = CanonicalNamespace.URI)
    private String text;

    public Reference() {
    }

    public Reference(String target, String text) {
        this.target = target;
        this.text = text;
    }

    public String getTarget() {
        return target;
    }

    public String getText() {
        return text;
    }
}

