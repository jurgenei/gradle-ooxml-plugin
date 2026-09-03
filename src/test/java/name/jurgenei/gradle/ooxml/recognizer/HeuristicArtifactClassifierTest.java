package name.jurgenei.gradle.ooxml.recognizer;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HeuristicArtifactClassifierTest {
    private final HeuristicArtifactClassifier classifier = new HeuristicArtifactClassifier();

    @Test
    void classifiesStructuredVectorAsDiagram() {
        ArtifactClassification classification = classifier.classify("word/media/image1.emf", new byte[]{1, 2, 3});

        assertEquals(VisualArtifactKind.DIAGRAM, classification.kind());
        assertTrue(classification.confidence() >= 0.90);
    }

    @Test
    void classifiesChartPathAsChart() {
        ArtifactClassification classification = classifier.classify("xl/charts/chart1.xml", new byte[]{1});

        assertEquals(VisualArtifactKind.CHART, classification.kind());
        assertTrue(classification.confidence() >= 0.90);
    }

    @Test
    void classifiesRasterAsDiagramFallback() {
        ArtifactClassification classification = classifier.classify("word/media/image1.png", new byte[]{1, 2, 3});

        assertEquals(VisualArtifactKind.DIAGRAM, classification.kind());
        assertEquals("raster-fallback", classification.evidence());
    }

    @Test
    void classifiesRasterWithChartHintsAsChart() {
        byte[] data = "axis legend series".getBytes(StandardCharsets.UTF_8);
        ArtifactClassification classification = classifier.classify("word/media/image2.png", data);

        assertEquals(VisualArtifactKind.CHART, classification.kind());
        assertEquals("raster-hints=chart", classification.evidence());
    }
}

