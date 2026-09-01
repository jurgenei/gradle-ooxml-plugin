package name.jurgenei.gradle.ooxml.recognizer;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.List;

import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PngAssetRecognizerTest {
    @Test
    void supportsPngByExtensionOrSignature() throws Exception {
        byte[] png = samplePngBytes();
        byte[] notPng = "plain-text".getBytes(StandardCharsets.UTF_8);

        PngAssetRecognizer recognizer = new PngAssetRecognizer(
                new ConfidenceModel(),
                new TextSnippetRecognizer(new ConfidenceModel()),
                image -> "",
                image -> image
        );

        assertTrue(recognizer.supports("png", notPng));
        assertTrue(recognizer.supports("bin", png));
        assertFalse(recognizer.supports("jpg", notPng));
    }

    @Test
    void recognizeUsesOcrTextForDiagramRecovery() throws Exception {
        byte[] png = samplePngBytes();

        PngAssetRecognizer recognizer = new PngAssetRecognizer(
                new ConfidenceModel(),
                new TextSnippetRecognizer(new ConfidenceModel()),
                image -> "Start | Validate Input | End",
                image -> image
        );

        AssetRecognition recognition = recognizer.recognize("asset-1", "word/media/image1.png", png);

        assertTrue(recognition.nodes().stream().anyMatch(node -> "Start".equals(node.getLabel())));
        assertTrue(recognition.nodes().stream().anyMatch(node -> "End".equals(node.getLabel())));
        assertTrue(recognition.annotations().stream().anyMatch(annotation -> "png-ocr".equals(annotation.getKind())));
    }

    @Test
    void recognizeRunsCvPreprocessorBeforeOcr() throws Exception {
        byte[] png = samplePngBytes();
        AtomicBoolean preprocessed = new AtomicBoolean(false);

        PngAssetRecognizer recognizer = new PngAssetRecognizer(
                new ConfidenceModel(),
                new TextSnippetRecognizer(new ConfidenceModel()),
                image -> preprocessed.get() ? "Start | Task | End" : "",
                image -> {
                    preprocessed.set(true);
                    return image;
                }
        );

        AssetRecognition recognition = recognizer.recognize("asset-2", "word/media/image2.png", png);

        assertTrue(preprocessed.get());
        assertTrue(recognition.nodes().size() >= 3);
        assertEquals(1, recognition.annotations().stream().filter(a -> "png-ocr".equals(a.getKind())).count());
    }

    @Test
    void recognizeSynthesizesBulletActionFlowFromPlantUmlText() throws Exception {
        byte[] png = samplePngBytes();

        PngAssetRecognizer recognizer = new PngAssetRecognizer(
                new ConfidenceModel(),
                new TextSnippetRecognizer(new ConfidenceModel()),
                image -> """
                        @startuml
                        * Action 1
                        * Action 2
                        * Action 3
                        @enduml
                        """,
                image -> image
        );

        AssetRecognition recognition = recognizer.recognize("asset-bullet", "word/media/image1.png", png);
        List<String> labels = recognition.nodes().stream().map(node -> node.getLabel()).toList();

        assertEquals(3, recognition.nodes().size());
        assertTrue(labels.contains("Action 1"));
        assertTrue(labels.contains("Action 2"));
        assertTrue(labels.contains("Action 3"));
        assertEquals(2, recognition.edges().size());
        assertTrue(hasEdge(recognition, "Action 1", "Action 2"));
        assertTrue(hasEdge(recognition, "Action 2", "Action 3"));
    }

    @Test
    void recognizeSynthesizesControlFlowFromPlantUmlLoopText() throws Exception {
        byte[] png = samplePngBytes();

        PngAssetRecognizer recognizer = new PngAssetRecognizer(
                new ConfidenceModel(),
                new TextSnippetRecognizer(new ConfidenceModel()),
                image -> """
                        @startuml
                        :Step 1;
                        if (condition1) then
                          while (loop forever)
                           :Step 2;
                          endwhile
                          -[hidden]->
                          detach
                        else
                          :end normally;
                          stop
                        endif
                        @enduml
                        """,
                image -> image
        );

        AssetRecognition recognition = recognizer.recognize("asset-loop", "word/media/image2.png", png);

        assertEquals(6, recognition.nodes().size());
        assertTrue(recognition.nodes().stream().anyMatch(node -> "Step 1".equals(node.getLabel())));
        assertTrue(recognition.nodes().stream().anyMatch(node -> "Step 2".equals(node.getLabel())));
        assertTrue(recognition.nodes().stream().anyMatch(node -> "condition1".equals(node.getLabel())));
        assertTrue(recognition.nodes().stream().anyMatch(node -> "loop forever".equals(node.getLabel())));
        assertTrue(recognition.nodes().stream().anyMatch(node -> "diamond".equals(node.getGeometry())));
        assertTrue(recognition.nodes().stream().anyMatch(node -> "end normally".equalsIgnoreCase(node.getLabel())));
        assertTrue(recognition.nodes().stream().anyMatch(node -> "Stop".equals(node.getLabel())));
        assertEquals(6, recognition.edges().size());
        assertTrue(hasEdge(recognition, "Step 1", "condition1"));
        assertTrue(hasEdge(recognition, "condition1", "loop forever"));
        assertTrue(hasEdge(recognition, "loop forever", "Step 2"));
        assertTrue(hasEdge(recognition, "Step 2", "loop forever"));
        assertTrue(hasEdge(recognition, "condition1", "end normally"));
        assertTrue(hasEdge(recognition, "end normally", "Stop"));
    }

    private boolean hasEdge(AssetRecognition recognition, String sourceLabel, String targetLabel) {
        Map<String, String> labelsById = recognition.nodes().stream()
                .collect(java.util.stream.Collectors.toMap(node -> node.getId(), node -> node.getLabel()));
        return recognition.edges().stream().anyMatch(edge ->
                sourceLabel.equals(labelsById.get(edge.getSource()))
                        && targetLabel.equals(labelsById.get(edge.getTarget()))
        );
    }

    private byte[] samplePngBytes() throws Exception {
        BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, 32, 32);
        graphics.setColor(Color.BLACK);
        graphics.drawRect(4, 4, 24, 24);
        graphics.dispose();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }
}
