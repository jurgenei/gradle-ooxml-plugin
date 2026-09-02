package name.jurgenei.gradle.ooxml.canonical;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import name.jurgenei.gradle.ooxml.CanonicalNamespace;

/**
 * Canonical list item text payload.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ListItem {
    @XmlElement(name = "text", namespace = CanonicalNamespace.URI, required = true)
    private String text;

    public ListItem() {
    }

    /**
     * Creates a list item with text content.
     *
     * @param text item text.
     */
    public ListItem(String text) {
        this.text = text;
    }

    /**
     * @return item text content.
     */
    public String getText() {
        return text;
    }
}

