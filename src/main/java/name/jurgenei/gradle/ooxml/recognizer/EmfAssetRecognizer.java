package name.jurgenei.gradle.ooxml.recognizer;

import name.jurgenei.gradle.ooxml.canonical.DiagramAnnotation;
import name.jurgenei.gradle.ooxml.canonical.DiagramEdge;
import name.jurgenei.gradle.ooxml.canonical.DiagramGroup;
import name.jurgenei.gradle.ooxml.canonical.DiagramNode;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Custom EMF recognizer that parses EMF records and reuses text-graph inference.
 */
public final class EmfAssetRecognizer implements AssetRecognizer {
    private static final int EMR_POLYBEZIER = 2;
    private static final int EMR_POLYLINE = 4;
    private static final int EMR_ELLIPSE = 42;
    private static final int EMR_RECTANGLE = 43;
    private static final int EMR_EXTTEXTOUTW = 84;

    private final ConfidenceModel confidenceModel;
    private final TextSnippetRecognizer fallbackTextRecognizer;

    public EmfAssetRecognizer() {
        this(new ConfidenceModel());
    }

    public EmfAssetRecognizer(ConfidenceModel confidenceModel) {
        this.confidenceModel = confidenceModel;
        this.fallbackTextRecognizer = new TextSnippetRecognizer(confidenceModel);
    }

    @Override
    public boolean supports(String extension, byte[] data) {
        return "emf".equals(extension) || hasEmfSignature(data);
    }

    @Override
    public AssetRecognition recognize(String assetId, String assetPath, byte[] data) {
        RecordStats stats = parseEmfRecords(data);
        String extractedText = stats.tokens().isEmpty() ? "" : String.join(" | ", stats.tokens());

        AssetRecognition base = extractedText.isBlank()
                ? fallbackTextRecognizer.recognize(assetId, assetPath, data)
                : fallbackTextRecognizer.recognize(assetId, assetPath, extractedText.getBytes(StandardCharsets.UTF_16LE));

        if (base.nodes().isEmpty() && base.edges().isEmpty()) {
            return base;
        }

        int sampleSize = Math.max(1, stats.textRecords() + stats.shapeRecords() + stats.connectorRecords());
        double nodeConfidence = confidenceModel.score(
                Math.min(base.nodes().size(), Math.max(1, stats.shapeRecords())),
                Math.max(1, base.nodes().size()),
                sampleSize
        );
        double edgeConfidence = confidenceModel.score(
                Math.min(base.edges().size(), Math.max(1, stats.connectorRecords())),
                Math.max(1, base.edges().size()),
                sampleSize
        );

        List<DiagramNode> calibratedNodes = new ArrayList<>();
        for (DiagramNode node : base.nodes()) {
            DiagramNode calibrated = new DiagramNode(node.getId(), node.getLabel(), node.getGeometry(), node.getSemantic(), node.getConfidence());
            if (node.getConfidence() == null) {
                calibrated.setConfidence(nodeConfidence);
            } else {
                calibrated.setConfidence(roundToTwoDecimals((node.getConfidence() + nodeConfidence) / 2.0));
            }
            calibratedNodes.add(calibrated);
        }

        List<DiagramEdge> calibratedEdges = new ArrayList<>();
        for (DiagramEdge edge : base.edges()) {
            double mergedConfidence = edge.getConfidence() == null
                    ? edgeConfidence
                    : roundToTwoDecimals((edge.getConfidence() + edgeConfidence) / 2.0);
            calibratedEdges.add(new DiagramEdge(edge.getSource(), edge.getTarget(), edge.getDirected(), edge.getSemantic(), mergedConfidence, edge.getLabel()));
        }

        List<DiagramAnnotation> annotations = new ArrayList<>(base.annotations());
        annotations.add(new DiagramAnnotation(
                "emf-stats",
                assetId,
                confidenceModel.score(3, 3, sampleSize),
                "records text=" + stats.textRecords() + ", shape=" + stats.shapeRecords() + ", connector=" + stats.connectorRecords()
        ));

        return new AssetRecognition(calibratedNodes, calibratedEdges, base.groups(), annotations);
    }

    private RecordStats parseEmfRecords(byte[] data) {
        LinkedHashSet<String> tokens = new LinkedHashSet<>();
        int textRecords = 0;
        int shapeRecords = 0;
        int connectorRecords = 0;

        int offset = 0;
        while (offset + 8 <= data.length) {
            int type = readIntLE(data, offset);
            int size = readIntLE(data, offset + 4);
            if (size < 8 || offset + size > data.length) {
                break;
            }

            if (type == EMR_EXTTEXTOUTW) {
                textRecords++;
                String payloadText = extractWideText(data, offset + 8, size - 8, 24);
                if (!payloadText.isBlank()) {
                    tokens.add(payloadText);
                }
            } else if (type == EMR_RECTANGLE || type == EMR_ELLIPSE) {
                shapeRecords++;
            } else if (type == EMR_POLYLINE || type == EMR_POLYBEZIER) {
                connectorRecords++;
            }

            offset += size;
        }

        // Some EMF writers keep Unicode strings outside strict EXTTEXTOUT payloads.
        if (tokens.isEmpty()) {
            String wideScan = extractWideText(data, 0, data.length, 24);
            if (!wideScan.isBlank()) {
                tokens.add(wideScan);
            }
        }

        List<String> normalized = tokens.stream().map(this::normalizeSnippet).filter(s -> !s.isBlank()).limit(24).toList();
        return new RecordStats(normalized, textRecords, shapeRecords, connectorRecords);
    }

    private String extractWideText(byte[] data, int start, int length, int tokenLimit) {
        int from = Math.max(0, start);
        int to = Math.min(data.length, start + Math.max(length, 0));
        if (from >= to) {
            return "";
        }

        String utf16 = new String(data, from, to - from, StandardCharsets.UTF_16LE);
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("[A-Za-z][A-Za-z0-9 ,._:/()=\\-*]{2,}");
        java.util.regex.Matcher matcher = pattern.matcher(utf16);
        LinkedHashSet<String> snippets = new LinkedHashSet<>();
        while (matcher.find() && snippets.size() < tokenLimit) {
            String token = matcher.group();
            if (token == null) {
                continue;
            }
            String normalized = normalizeSnippet(token);
            if (normalized.length() >= 4 && normalized.chars().filter(Character::isLetter).count() >= 3) {
                snippets.add(normalized);
            }
        }
        return String.join(" | ", snippets);
    }

    private String normalizeSnippet(String token) {
        String normalized = token.replaceAll("\\s+", " ").trim();
        if (normalized.startsWith("ING Me")) {
            return "";
        }
        return normalized;
    }

    private boolean hasEmfSignature(byte[] data) {
        if (data.length < 44) {
            return false;
        }
        return data[40] == 0x20 && data[41] == 0x45 && data[42] == 0x4D && data[43] == 0x46;
    }

    private int readIntLE(byte[] data, int offset) {
        if (offset + 4 > data.length) {
            return 0;
        }
        return (data[offset] & 0xFF)
                | ((data[offset + 1] & 0xFF) << 8)
                | ((data[offset + 2] & 0xFF) << 16)
                | ((data[offset + 3] & 0xFF) << 24);
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private record RecordStats(List<String> tokens,
                               int textRecords,
                               int shapeRecords,
                               int connectorRecords) {
    }
}

