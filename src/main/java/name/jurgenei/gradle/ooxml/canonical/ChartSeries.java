package name.jurgenei.gradle.ooxml.canonical;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import name.jurgenei.gradle.ooxml.CanonicalNamespace;

import java.util.ArrayList;
import java.util.List;

/**
 * Canonical chart series descriptor.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ChartSeries {
    @XmlElement(name = "name", namespace = CanonicalNamespace.URI)
    private String name;

    @XmlElement(name = "value", namespace = CanonicalNamespace.URI)
    private List<String> values = new ArrayList<>();

    public ChartSeries() {
    }

    public ChartSeries(String name, List<String> values) {
        this.name = name;
        this.values = values;
    }

    public String getName() {
        return name;
    }

    public List<String> getValues() {
        return values;
    }
}

