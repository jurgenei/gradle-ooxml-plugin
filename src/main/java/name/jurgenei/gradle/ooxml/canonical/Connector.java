package name.jurgenei.gradle.ooxml.canonical;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

/**
 * Canonical diagram connector edge.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Connector {
    @XmlAttribute(name = "source")
    private String source;

    @XmlAttribute(name = "target")
    private String target;

    public Connector() {
    }

    /**
     * Creates a connector between two shapes.
     *
     * @param source source shape id.
     * @param target target shape id.
     */
    public Connector(String source, String target) {
        this.source = source;
        this.target = target;
    }

    /**
     * @return source shape id.
     */
    public String getSource() {
        return source;
    }

    /**
     * @return target shape id.
     */
    public String getTarget() {
        return target;
    }
}

