package name.jurgenei.gradle.ooxml.recognizer;

import name.jurgenei.gradle.ooxml.canonical.DiagramAnnotation;
import name.jurgenei.gradle.ooxml.canonical.DiagramEdge;
import name.jurgenei.gradle.ooxml.canonical.DiagramGroup;
import name.jurgenei.gradle.ooxml.canonical.DiagramNode;

import java.util.List;

/**
 * Result of format-specific diagram recognition.
 */
public record AssetRecognition(List<DiagramNode> nodes,
                               List<DiagramEdge> edges,
                               List<DiagramGroup> groups,
                               List<DiagramAnnotation> annotations) {
    public static AssetRecognition empty() {
        return new AssetRecognition(List.of(), List.of(), List.of(), List.of());
    }
}

