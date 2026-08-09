package name.jurgenei.gradle.ooxml.canonical;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import name.jurgenei.gradle.ooxml.CanonicalNamespace;

/**
 * Canonical table cell text payload.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Cell {
    @XmlElement(name = "Text", namespace = CanonicalNamespace.URI, required = true)
    private String text;

    public Cell() {
    }

    /**
     * Creates a cell with text content.
     *
     * @param text cell text.
     */
    public Cell(String text) {
        this.text = text;
    }

    /**
     * @return cell text content.
     */
    public String getText() {
        return text;
    }
}

