package name.jurgenei.gradle.ooxml.canonical;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import name.jurgenei.gradle.ooxml.CanonicalNamespace;

import java.util.ArrayList;
import java.util.List;

/**
 * Canonical list container with ordered/unordered semantics.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class CanonicalList {
    @XmlAttribute(name = "ordered")
    private boolean ordered;

    @XmlElement(name = "Item", namespace = CanonicalNamespace.URI)
    private List<ListItem> items = new ArrayList<>();

    public CanonicalList() {
    }

    /**
     * Creates a canonical list with explicit ordering and items.
     *
     * @param ordered whether list order is semantically significant.
     * @param items canonical list items.
     */
    public CanonicalList(boolean ordered, List<ListItem> items) {
        this.ordered = ordered;
        this.items = items;
    }

    /**
     * @return {@code true} for ordered lists, otherwise {@code false}.
     */
    public boolean isOrdered() {
        return ordered;
    }

    /**
     * @return list items in source-relative order.
     */
    public List<ListItem> getItems() {
        return items;
    }
}

