package name.jurgenei.gradle.ooxml.canonical;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import name.jurgenei.gradle.ooxml.CanonicalNamespace;

import java.util.ArrayList;
import java.util.List;

/**
 * Canonical grouping construct for related diagram nodes.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class DiagramGroup {
    @XmlAttribute(name = "id")
    private String id;

    @XmlAttribute(name = "semantic")
    private String semantic;

    @XmlElement(name = "Label", namespace = CanonicalNamespace.URI)
    private String label;

    @XmlElement(name = "Member", namespace = CanonicalNamespace.URI)
    private List<DiagramGroupMember> members = new ArrayList<>();

    public DiagramGroup() {
    }

    public DiagramGroup(String id, String semantic, String label, List<DiagramGroupMember> members) {
        this.id = id;
        this.semantic = semantic;
        this.label = label;
        this.members = members;
    }

    public String getId() {
        return id;
    }

    public String getSemantic() {
        return semantic;
    }

    public String getLabel() {
        return label;
    }

    public List<DiagramGroupMember> getMembers() {
        return members;
    }
}

