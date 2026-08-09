package name.jurgenei.gradle.ooxml.canonical;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import name.jurgenei.gradle.ooxml.CanonicalNamespace;

import java.util.ArrayList;
import java.util.List;

/**
 * Canonical table container.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Table {
    @XmlElement(name = "Row", namespace = CanonicalNamespace.URI)
    private List<Row> rows = new ArrayList<>();

    public Table() {
    }

    /**
     * Creates a table with pre-populated row content.
     *
     * @param rows canonical rows.
     */
    public Table(List<Row> rows) {
        this.rows = rows;
    }

    /**
     * @return canonical rows in document order.
     */
    public List<Row> getRows() {
        return rows;
    }
}

