package name.jurgenei.gradle.ooxml.recognizer;

/**
 * Classification outcome for one visual asset.
 */
public record ArtifactClassification(VisualArtifactKind kind, double confidence, String evidence) {
}

