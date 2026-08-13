package name.jurgenei.gradle.ooxml.canonical;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAnyElement;
import jakarta.xml.bind.annotation.XmlElement;
import name.jurgenei.gradle.ooxml.CanonicalNamespace;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Canonical table cell text payload.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Cell {
    @XmlElement(name = "Text", namespace = CanonicalNamespace.URI)
    private List<String> texts = new ArrayList<>();

    @XmlAnyElement
    private List<Element> math = new ArrayList<>();

    public Cell() {
    }

    /**
     * Creates a cell with text content.
     *
     * @param text cell text.
     */
    public Cell(String text) {
        addText(text);
    }

    /**
     * @return cell text content.
     */
    public String getText() {
        if (texts.isEmpty()) {
            return "";
        }
        return String.join(" ", texts).trim();
    }

    public List<Element> getMath() {
        return math;
    }

    public void addText(String text) {
        if (text != null && !text.isBlank()) {
            texts.add(text.trim());
        }
    }

    public void addMath(Element mathElement) {
        if (mathElement != null) {
            math.add(mathElement);
        }
    }

    public boolean hasContent() {
        return !texts.isEmpty() || !math.isEmpty();
    }
}

