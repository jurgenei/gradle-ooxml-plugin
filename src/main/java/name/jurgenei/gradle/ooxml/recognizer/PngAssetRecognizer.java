package name.jurgenei.gradle.ooxml.recognizer;

import name.jurgenei.gradle.ooxml.canonical.DiagramAnnotation;
import name.jurgenei.gradle.ooxml.canonical.DiagramEdge;
import name.jurgenei.gradle.ooxml.canonical.DiagramNode;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import net.sourceforge.tess4j.Tesseract;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * PNG recognizer that combines OpenCV pre-processing with Tess4J OCR.
 */
public final class PngAssetRecognizer implements AssetRecognizer {
    private static final byte[] PNG_SIGNATURE = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

    private final ConfidenceModel confidenceModel;
    private final TextSnippetRecognizer textSnippetRecognizer;
    private final OcrEngine ocrEngine;
    private final CvPreprocessor cvPreprocessor;

    public PngAssetRecognizer() {
        this(new ConfidenceModel());
    }

    public PngAssetRecognizer(ConfidenceModel confidenceModel) {
        this(confidenceModel, new TextSnippetRecognizer(confidenceModel), new Tess4jOcrEngine(), new OpenCvPreprocessor());
    }

    PngAssetRecognizer(ConfidenceModel confidenceModel,
                       TextSnippetRecognizer textSnippetRecognizer,
                       OcrEngine ocrEngine,
                       CvPreprocessor cvPreprocessor) {
        this.confidenceModel = confidenceModel;
        this.textSnippetRecognizer = textSnippetRecognizer;
        this.ocrEngine = ocrEngine;
        this.cvPreprocessor = cvPreprocessor;
    }

    @Override
    public boolean supports(String extension, byte[] data) {
        return "png".equals(extension) || hasPngSignature(data);
    }

    @Override
    public AssetRecognition recognize(String assetId, String assetPath, byte[] data) {
        BufferedImage image = decodeImage(data);
        if (image == null) {
            return AssetRecognition.empty();
        }

        BufferedImage prepared = cvPreprocessor.preprocess(image);
        String ocrText = normalizeText(ocrEngine.readText(prepared));
        if (ocrText.isBlank()) {
            ocrText = normalizeText(ocrEngine.readText(image));
        }

        AssetRecognition base = ocrText.isBlank()
                ? AssetRecognition.empty()
                : textSnippetRecognizer.recognize(assetId, assetPath, ocrText.getBytes(StandardCharsets.UTF_16LE));

        AssetRecognition synthesized = synthesizeFromOcrTokens(assetId, ocrText);
        AssetRecognition effective = synthesized.nodes().size() > base.nodes().size() ? synthesized : base;

        List<DiagramAnnotation> annotations = new ArrayList<>(effective.annotations());
        annotations.add(new DiagramAnnotation(
                "png-ocr",
                assetId,
                ocrConfidence(ocrText),
                ocrText
        ));
        annotations.add(new DiagramAnnotation(
                "png-stats",
                assetId,
                confidenceModel.score(2, 2, Math.max(1, wordCount(ocrText))),
                "size=" + image.getWidth() + "x" + image.getHeight() + ", textChars=" + ocrText.length()
        ));

        return new AssetRecognition(effective.nodes(), effective.edges(), effective.groups(), annotations);
    }

    private AssetRecognition synthesizeFromOcrTokens(String assetId, String ocrText) {
        if (ocrText == null || ocrText.isBlank()) {
            return AssetRecognition.empty();
        }

        List<String> tokens = new ArrayList<>();
        for (String raw : ocrText.split("[\\|\\r\\n]+")) {
            String token = normalizeText(raw);
            if (!token.isBlank()) {
                tokens.add(token);
            }
        }
        if (tokens.size() < 2) {
            return AssetRecognition.empty();
        }

        List<DiagramNode> nodes = new ArrayList<>();
        List<DiagramEdge> edges = new ArrayList<>();
        List<String> nodeIds = new ArrayList<>();

        for (int i = 0; i < Math.min(tokens.size(), 16); i++) {
            String label = tokens.get(i);
            String nodeId = assetId + "-png-" + i;
            String lower = label.toLowerCase(Locale.ROOT);
            String geometry = ("start".equals(lower) || "end".equals(lower)) ? "ellipse"
                    : lower.startsWith("if ") ? "diamond" : "rectangle";
            String semantic = ("start".equals(lower)) ? "root"
                    : ("end".equals(lower)) ? "leaf"
                    : lower.startsWith("if ") ? "decision" : "process";
            nodes.add(new DiagramNode(
                    nodeId,
                    label,
                    geometry,
                    semantic,
                    confidenceModel.score(2, 3, tokens.size())
            ));
            nodeIds.add(nodeId);
        }

        for (int i = 0; i + 1 < nodeIds.size(); i++) {
            edges.add(new DiagramEdge(
                    nodeIds.get(i),
                    nodeIds.get(i + 1),
                    true,
                    "flow",
                    confidenceModel.score(1, 1, nodeIds.size()),
                    null
            ));
        }

        return new AssetRecognition(nodes, edges, List.of(), List.of());
    }

    private Double ocrConfidence(String text) {
        int words = wordCount(text);
        if (words == 0) {
            return 0.40;
        }
        return confidenceModel.score(Math.min(words, 8), 8, words);
    }

    private int wordCount(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        return text.trim().split("\\s+").length;
    }

    private BufferedImage decodeImage(byte[] data) {
        try {
            return ImageIO.read(new ByteArrayInputStream(data));
        } catch (Exception ignored) {
            return null;
        }
    }

    private boolean hasPngSignature(byte[] data) {
        if (data == null || data.length < PNG_SIGNATURE.length) {
            return false;
        }
        for (int i = 0; i < PNG_SIGNATURE.length; i++) {
            if (data[i] != PNG_SIGNATURE[i]) {
                return false;
            }
        }
        return true;
    }

    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("\\s+", " ").trim();
    }

    @FunctionalInterface
    interface OcrEngine {
        String readText(BufferedImage image);
    }

    @FunctionalInterface
    interface CvPreprocessor {
        BufferedImage preprocess(BufferedImage image);
    }

    static final class Tess4jOcrEngine implements OcrEngine {
        @Override
        public String readText(BufferedImage image) {
            if (image == null) {
                return "";
            }
            try {
                Tesseract tesseract = new Tesseract();
                tesseract.setLanguage("eng");
                return tesseract.doOCR(image);
            } catch (Throwable ignored) {
                return "";
            }
        }
    }

    static final class OpenCvPreprocessor implements CvPreprocessor {
        private static final boolean OPENCV_AVAILABLE = loadOpenCv();

        @Override
        public BufferedImage preprocess(BufferedImage image) {
            if (image == null || !OPENCV_AVAILABLE) {
                return image;
            }
            try {
                byte[] sourcePng = toPngBytes(image);
                Mat decoded = Imgcodecs.imdecode(new MatOfByte(sourcePng), Imgcodecs.IMREAD_GRAYSCALE);
                if (decoded.empty()) {
                    return image;
                }

                Mat denoised = new Mat();
                Imgproc.medianBlur(decoded, denoised, 3);
                Mat binary = new Mat();
                Imgproc.threshold(denoised, binary, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);

                MatOfByte output = new MatOfByte();
                Imgcodecs.imencode(".png", binary, output);
                BufferedImage processed = ImageIO.read(new ByteArrayInputStream(output.toArray()));

                decoded.release();
                denoised.release();
                binary.release();
                output.release();
                return processed == null ? image : processed;
            } catch (Exception ignored) {
                return image;
            }
        }

        private static boolean loadOpenCv() {
            try {
                nu.pattern.OpenCV.loadLocally();
                return true;
            } catch (Throwable ignored) {
                try {
                    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
                    return true;
                } catch (Throwable secondIgnored) {
                    return false;
                }
            }
        }

        private byte[] toPngBytes(BufferedImage image) throws Exception {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(image, "png", output);
            return output.toByteArray();
        }
    }
}

