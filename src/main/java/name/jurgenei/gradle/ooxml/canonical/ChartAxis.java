package name.jurgenei.gradle.ooxml.canonical;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import name.jurgenei.gradle.ooxml.CanonicalNamespace;

/**
 * Canonical chart axis descriptor.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ChartAxis {
    @XmlAttribute(name = "role")
    private String role;

    @XmlElement(name = "label", namespace = CanonicalNamespace.URI)
    private String label;

    @XmlElement(name = "unit", namespace = CanonicalNamespace.URI)
    private String unit;

    public ChartAxis() {
    }

    public ChartAxis(String role, String label, String unit) {
        this.role = role;
        this.label = label;
        this.unit = unit;
    }

    public String getRole() {
        return role;
    }

    public String getLabel() {
        return label;
    }

    public String getUnit() {
        return unit;
    }
}

