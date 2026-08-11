package name.jurgenei.gradle.ooxml.canonical;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlAttribute;
import name.jurgenei.gradle.ooxml.CanonicalNamespace;

import java.util.ArrayList;
import java.util.List;

/**
 * Canonical table container.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Table {
    @XmlAttribute(name = "id")
    private String id;

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
     * Creates a table with a logical identifier, for example an XLSX worksheet name.
     *
     * @param id logical table identifier.
     * @param rows canonical rows.
     */
    public Table(String id, List<Row> rows) {
        this.id = id;
        this.rows = rows;
    }

    public String getId() {
        return id;
    }

    /**
     * @return canonical rows in document order.
     */
    public List<Row> getRows() {
        return rows;
    }
}

