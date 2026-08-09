package name.jurgenei.gradle.ooxml.canonical;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import name.jurgenei.gradle.ooxml.CanonicalNamespace;

import java.util.ArrayList;
import java.util.List;

/**
 * Canonical body container aggregating normalized structural and relational content.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Body {
    @XmlElement(name = "Paragraph", namespace = CanonicalNamespace.URI)
    private List<Paragraph> paragraphs = new ArrayList<>();

    @XmlElement(name = "List", namespace = CanonicalNamespace.URI)
    private List<CanonicalList> lists = new ArrayList<>();

    @XmlElement(name = "Table", namespace = CanonicalNamespace.URI)
    private List<Table> tables = new ArrayList<>();

    @XmlElement(name = "Link", namespace = CanonicalNamespace.URI)
    private List<Link> links = new ArrayList<>();

    @XmlElement(name = "Reference", namespace = CanonicalNamespace.URI)
    private List<Reference> references = new ArrayList<>();

    @XmlElement(name = "Diagram", namespace = CanonicalNamespace.URI)
    private List<Diagram> diagrams = new ArrayList<>();

    public Body() {
    }

    public Body(List<Paragraph> paragraphs) {
        this.paragraphs = paragraphs;
    }

    public Body(List<Paragraph> paragraphs, List<CanonicalList> lists, List<Table> tables) {
        this.paragraphs = paragraphs;
        this.lists = lists;
        this.tables = tables;
    }

    public Body(List<Paragraph> paragraphs,
                List<CanonicalList> lists,
                List<Table> tables,
                List<Link> links,
                List<Reference> references,
                List<Diagram> diagrams) {
        this.paragraphs = paragraphs;
        this.lists = lists;
        this.tables = tables;
        this.links = links;
        this.references = references;
        this.diagrams = diagrams;
    }

    public List<Paragraph> getParagraphs() {
        return paragraphs;
    }

    public List<CanonicalList> getLists() {
        return lists;
    }

    public List<Table> getTables() {
        return tables;
    }

    public List<Link> getLinks() {
        return links;
    }

    public List<Reference> getReferences() {
        return references;
    }

    public List<Diagram> getDiagrams() {
        return diagrams;
    }
}

