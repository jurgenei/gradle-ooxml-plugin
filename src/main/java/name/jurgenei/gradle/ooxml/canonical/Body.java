package name.jurgenei.gradle.ooxml.canonical;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElementRef;
import jakarta.xml.bind.annotation.XmlElementRefs;
import name.jurgenei.gradle.ooxml.CanonicalNamespace;

import java.util.ArrayList;
import java.util.List;

/**
 * Canonical body container aggregating normalized structural and relational content.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Body {
    @XmlElementRefs({
            @XmlElementRef(name = "para", namespace = CanonicalNamespace.URI, type = Paragraph.class),
            @XmlElementRef(name = "list", namespace = CanonicalNamespace.URI, type = CanonicalList.class),
            @XmlElementRef(name = "table", namespace = CanonicalNamespace.URI, type = Table.class),
            @XmlElementRef(name = "link", namespace = CanonicalNamespace.URI, type = Link.class),
            @XmlElementRef(name = "reference", namespace = CanonicalNamespace.URI, type = Reference.class),
            @XmlElementRef(name = "chart", namespace = CanonicalNamespace.URI, type = Chart.class),
            @XmlElementRef(name = "graph", namespace = CanonicalNamespace.GRAPHML_URI, type = Diagram.class)
    })
    private List<Object> content = new ArrayList<>();

    public Body() {
    }

    public Body(List<Paragraph> paragraphs) {
        this.content.addAll(paragraphs);
    }

    public Body(List<Paragraph> paragraphs, List<CanonicalList> lists, List<Table> tables) {
        this.content.addAll(paragraphs);
        this.content.addAll(lists);
        this.content.addAll(tables);
    }

    public Body(List<Paragraph> paragraphs,
                List<CanonicalList> lists,
                List<Table> tables,
                List<Link> links,
                List<Reference> references,
                List<Diagram> diagrams) {
        this.content.addAll(paragraphs);
        this.content.addAll(lists);
        this.content.addAll(tables);
        this.content.addAll(links);
        this.content.addAll(references);
        this.content.addAll(diagrams);
    }

    public static Body ordered(List<Object> content) {
        Body body = new Body();
        body.content = content;
        return body;
    }

    public List<Paragraph> getParagraphs() {
        return filterByType(Paragraph.class);
    }

    public List<CanonicalList> getLists() {
        return filterByType(CanonicalList.class);
    }

    public List<Table> getTables() {
        return filterByType(Table.class);
    }

    public List<Link> getLinks() {
        return filterByType(Link.class);
    }

    public List<Reference> getReferences() {
        return filterByType(Reference.class);
    }

    public List<Chart> getCharts() {
        return filterByType(Chart.class);
    }

    public List<Diagram> getDiagrams() {
        return filterByType(Diagram.class);
    }


    public List<Object> getContent() {
        return content;
    }

    private <T> List<T> filterByType(Class<T> type) {
        List<T> values = new ArrayList<>();
        for (Object element : content) {
            if (type.isInstance(element)) {
                values.add(type.cast(element));
            }
        }
        return values;
    }
}

