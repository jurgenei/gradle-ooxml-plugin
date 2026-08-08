package name.jurgenei.gradle.ooxml.canonical;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import name.jurgenei.gradle.ooxml.CanonicalNamespace;

@XmlAccessorType(XmlAccessType.FIELD)
public class ListItem {
    @XmlElement(name = "Text", namespace = CanonicalNamespace.URI, required = true)
    private String text;

    public ListItem() {
    }

    public ListItem(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}

