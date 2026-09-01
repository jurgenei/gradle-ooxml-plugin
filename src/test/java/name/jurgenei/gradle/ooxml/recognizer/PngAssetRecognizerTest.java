package name.jurgenei.gradle.ooxml.recognizer;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

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

