package name.jurgenei.gradle.ooxml.recognizer;

/**
 * Classifies visual artifact by semantic kind before specialized extraction.
 */
@FunctionalInterface
public interface ArtifactClassifier {
    ArtifactClassification classify(String assetPath, byte[] data);
}

