package name.jurgenei.gradle.ooxml.canonical;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import name.jurgenei.gradle.ooxml.CanonicalNamespace;

import java.util.ArrayList;
import java.util.List;

/**
 * Canonical table row.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Row {
    @XmlElement(name = "Cell", namespace = CanonicalNamespace.URI)
    private List<Cell> cells = new ArrayList<>();

    public Row() {
    }

    /**
     * Creates a row with pre-populated cell content.
     *
     * @param cells canonical cells.
     */
    public Row(List<Cell> cells) {
        this.cells = cells;
    }

    /**
     * @return row cells in source order.
     */
    public List<Cell> getCells() {
        return cells;
    }
}

