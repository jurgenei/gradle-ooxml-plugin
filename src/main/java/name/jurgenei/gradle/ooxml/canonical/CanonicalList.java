package name.jurgenei.gradle.ooxml.canonical;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import name.jurgenei.gradle.ooxml.CanonicalNamespace;

import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class CanonicalList {
    @XmlAttribute(name = "ordered")
    private boolean ordered;

    @XmlElement(name = "Item", namespace = CanonicalNamespace.URI)
    private List<ListItem> items = new ArrayList<>();

    public CanonicalList() {
    }

    public CanonicalList(boolean ordered, List<ListItem> items) {
        this.ordered = ordered;
        this.items = items;
    }

    public boolean isOrdered() {
        return ordered;
    }

    public List<ListItem> getItems() {
        return items;
    }
}

