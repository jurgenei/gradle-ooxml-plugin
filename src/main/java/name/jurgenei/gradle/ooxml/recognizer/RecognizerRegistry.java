package name.jurgenei.gradle.ooxml.recognizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ServiceLoader;

/**
 * Chooses recognizer implementation based on asset format.
 */
public final class RecognizerRegistry {
    private final List<AssetRecognizer> recognizers;
    private final AssetRecognizer fallback;

    public RecognizerRegistry(List<AssetRecognizer> recognizers, AssetRecognizer fallback) {
        this.recognizers = recognizers;
        this.fallback = fallback;
    }

    public static RecognizerRegistry defaultRegistry() {
        return defaultRegistry(List.of());
    }

    public static RecognizerRegistry defaultRegistry(List<AssetRecognizer> additionalRecognizers) {
        ConfidenceModel confidenceModel = new ConfidenceModel();
        List<AssetRecognizer> loaded = new ArrayList<>();
        ServiceLoader.load(AssetRecognizer.class).forEach(loaded::add);
        if (loaded.stream().noneMatch(recognizer -> recognizer.getClass().equals(EmfAssetRecognizer.class))) {
            loaded.add(new EmfAssetRecognizer(confidenceModel));
        }
        loaded.addAll(additionalRecognizers);
        AssetRecognizer fallback = new TextSnippetRecognizer(confidenceModel);
        return new RecognizerRegistry(List.copyOf(loaded), fallback);
    }

    public AssetRecognition recognize(String assetId, String assetPath, byte[] data) {
        String extension = extension(assetPath);
        for (AssetRecognizer recognizer : recognizers) {
            if (recognizer.supports(extension, data)) {
                return recognizer.recognize(assetId, assetPath, data);
            }
        }
        return fallback.recognize(assetId, assetPath, data);
    }

    private String extension(String path) {
        int dot = path.lastIndexOf('.');
        if (dot < 0 || dot + 1 >= path.length()) {
            return "";
        }
        return path.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}

