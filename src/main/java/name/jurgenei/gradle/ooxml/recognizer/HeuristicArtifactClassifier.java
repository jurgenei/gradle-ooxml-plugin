package name.jurgenei.gradle.ooxml.recognizer;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Deterministic classifier used until source-priority extractors are fully integrated.
 */
public final class HeuristicArtifactClassifier implements ArtifactClassifier {
    @Override
    public ArtifactClassification classify(String assetPath, byte[] data) {
        String path = assetPath == null ? "" : assetPath.toLowerCase(Locale.ROOT);
        String extension = extension(path);

        if (path.contains("/charts/") || path.contains("chart")) {
            return new ArtifactClassification(VisualArtifactKind.CHART, 0.92, "path=chart");
        }
        if ("emf".equals(extension) || "wmf".equals(extension) || "vsdx".equals(extension)
                || "svg".equals(extension) || "vml".equals(extension)) {
            return new ArtifactClassification(VisualArtifactKind.DIAGRAM, 0.90, "structured-vector");
        }
        if ("png".equals(extension) || "jpg".equals(extension) || "jpeg".equals(extension)) {
            if (path.contains("screenshot")) {
                return new ArtifactClassification(VisualArtifactKind.SCREENSHOT, 0.80, "path=screenshot");
            }
            if (looksLikeChartHint(data)) {
                return new ArtifactClassification(VisualArtifactKind.CHART, 0.70, "raster-hints=chart");
            }
            return new ArtifactClassification(VisualArtifactKind.DIAGRAM, 0.65, "raster-fallback");
        }
        return new ArtifactClassification(VisualArtifactKind.MIXED, 0.50, "unknown");
    }

    private boolean looksLikeChartHint(byte[] data) {
        if (data == null || data.length == 0) {
            return false;
        }
        int sampleSize = Math.min(data.length, 2048);
        String preview = new String(data, 0, sampleSize, StandardCharsets.ISO_8859_1).toLowerCase(Locale.ROOT);
        return preview.contains("axis") || preview.contains("legend") || preview.contains("series");
    }

    private String extension(String path) {
        int dot = path.lastIndexOf('.');
        if (dot < 0 || dot + 1 >= path.length()) {
            return "";
        }
        return path.substring(dot + 1);
    }
}

