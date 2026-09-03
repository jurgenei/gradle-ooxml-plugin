package name.jurgenei.gradle.ooxml.canonical;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import name.jurgenei.gradle.ooxml.CanonicalNamespace;

import java.util.ArrayList;
import java.util.List;

/**
 * Canonical chart evidence container.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "chart", namespace = CanonicalNamespace.URI)
public class Chart {
    @XmlElement(name = "title", namespace = CanonicalNamespace.URI)
    private String title;

    @XmlElement(name = "legend", namespace = CanonicalNamespace.URI)
    private String legend;

    @XmlElement(name = "axis", namespace = CanonicalNamespace.URI)
    private List<ChartAxis> axes = new ArrayList<>();

    @XmlElement(name = "series", namespace = CanonicalNamespace.URI)
    private List<ChartSeries> series = new ArrayList<>();

    @XmlAttribute(name = "source-path")
    private String sourcePath;

    @XmlAttribute(name = "href")
    private String href;

    public Chart() {
    }

    public Chart(String title,
                 String legend,
                 List<ChartAxis> axes,
                 List<ChartSeries> series,
                 String sourcePath,
                 String href) {
        this.title = title;
        this.legend = legend;
        this.axes = axes;
        this.series = series;
        this.sourcePath = sourcePath;
        this.href = href;
    }

    public String getTitle() {
        return title;
    }

    public String getLegend() {
        return legend;
    }

    public List<ChartAxis> getAxes() {
        return axes;
    }

    public List<ChartSeries> getSeries() {
        return series;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public String getHref() {
        return href;
    }
}

