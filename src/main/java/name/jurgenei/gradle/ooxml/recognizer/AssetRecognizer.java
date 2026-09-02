package name.jurgenei.gradle.ooxml.recognizer;

/**
 * Recognizes canonical diagram structure from binary asset payloads.
 */
public interface AssetRecognizer {
    boolean supports(String extension, byte[] data);

    AssetRecognition recognize(String assetId, String assetPath, byte[] data);
}

