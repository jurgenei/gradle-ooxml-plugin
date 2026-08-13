package name.jurgenei.gradle.ooxml.canonical;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

/**
 * Group membership reference to a canonical diagram node id.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class DiagramGroupMember {
    @XmlAttribute(name = "node")
    private String node;

    public DiagramGroupMember() {
    }

    public DiagramGroupMember(String node) {
        this.node = node;
    }

    public String getNode() {
        return node;
    }
}

