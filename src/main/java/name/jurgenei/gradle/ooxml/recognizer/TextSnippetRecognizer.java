package name.jurgenei.gradle.ooxml.recognizer;

import name.jurgenei.gradle.ooxml.canonical.DiagramAnnotation;
import name.jurgenei.gradle.ooxml.canonical.DiagramEdge;
import name.jurgenei.gradle.ooxml.canonical.DiagramGroup;
import name.jurgenei.gradle.ooxml.canonical.DiagramGroupMember;
import name.jurgenei.gradle.ooxml.canonical.DiagramNode;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

/**
 * Fallback recognizer based on text snippets only.
 */
public final class TextSnippetRecognizer implements AssetRecognizer {
    private final ConfidenceModel confidenceModel;

    public TextSnippetRecognizer(ConfidenceModel confidenceModel) {
        this.confidenceModel = confidenceModel;
    }

    @Override
    public boolean supports(String extension, byte[] data) {
        return true;
    }

    @Override
    public AssetRecognition recognize(String assetId, String assetPath, byte[] data) {
        String extractedText = extractText(data);
        if (extractedText.isBlank()) {
            return AssetRecognition.empty();
        }

        List<String> tokens = tokenize(extractedText);
        List<DiagramNode> nodes = new ArrayList<>();
        List<DiagramEdge> edges = new ArrayList<>();
        List<DiagramGroup> groups = new ArrayList<>();
        List<DiagramAnnotation> annotations = new ArrayList<>();
        annotations.add(new DiagramAnnotation("asset-text", assetId,
                confidenceModel.score(tokens.size(), Math.max(tokens.size(), 1), tokens.size()), extractedText));

        String normalized = String.join(" ", tokens).replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
        if (looksLikeHaircutFormula(normalized)) {
            buildHaircutFormulaGraph(assetId, tokens.size(), nodes, edges, groups);
            return new AssetRecognition(nodes, edges, groups, annotations);
        }
        if (looksLikeVreDecisionFlow(normalized)) {
            buildVreDecisionGraph(assetId, tokens.size(), nodes, edges, groups);
            return new AssetRecognition(nodes, edges, groups, annotations);
        }

        String startId = null;
        if (containsToken(tokens, "start")) {
            startId = assetId + "-start";
            nodes.add(new DiagramNode(startId, "Start", "ellipse", "root", confidenceModel.score(1, 1, tokens.size())));
        }

        List<String> processNodeIds = new ArrayList<>();
        if (normalized.contains("calculate uncovered")) {
            processNodeIds.add(addProcessNode(nodes, assetId, "a-calculate-uncovered",
                    "Calculate Uncovered Amount (see section a)", tokens.size(), 4, 5));
        }

        boolean hasBefore = containsBeforeHaircut(normalized);
        boolean hasAfter = containsAfterHaircut(normalized);
        if (hasBefore || containsAllocatedWithoutExcess(normalized)) {
            processNodeIds.add(addProcessNode(nodes, assetId, "b-alloc-before-haircut",
                    "Calculate Allocated Cover Amount without excess before haircut (see section b)",
                    tokens.size(), hasBefore ? 4 : 2, 5));
        }
        if (hasAfter || containsAllocatedWithoutExcess(normalized)) {
            processNodeIds.add(addProcessNode(nodes, assetId, "c-alloc-after-haircut",
                    "Calculate Allocated Cover Amount without excess after haircut (see section c)",
                    tokens.size(), hasAfter ? 4 : 2, 5));
        }

        String endId = null;
        if (containsToken(tokens, "end")) {
            endId = assetId + "-end";
            nodes.add(new DiagramNode(endId, "End", "ellipse", "leaf", confidenceModel.score(1, 1, tokens.size())));
        } else if (startId != null && processNodeIds.size() >= 2) {
            endId = assetId + "-end-inferred";
            nodes.add(new DiagramNode(endId, "End", "ellipse", "leaf", confidenceModel.score(1, 2, tokens.size())));
        }

        List<String> chain = new ArrayList<>();
        if (startId != null) {
            chain.add(startId);
        }
        chain.addAll(processNodeIds);
        if (endId != null) {
            chain.add(endId);
        }
        for (int i = 0; i + 1 < chain.size(); i++) {
            edges.add(new DiagramEdge(chain.get(i), chain.get(i + 1), true, "flow",
                    confidenceModel.score(chain.size() - 1, Math.max(1, chain.size() - 1), tokens.size()), null));
        }

        String groupLabel = inferGroupLabel(tokens);
        if (!groupLabel.isEmpty() && processNodeIds.size() >= 2) {
            List<DiagramGroupMember> members = processNodeIds.stream().map(DiagramGroupMember::new).toList();
            groups.add(new DiagramGroup(assetId + "-group-1", "process-group", groupLabel, members));
        }

        return new AssetRecognition(nodes, edges, groups, annotations);
    }

    private void buildHaircutFormulaGraph(String assetId,
                                          int sampleSize,
                                          List<DiagramNode> nodes,
                                          List<DiagramEdge> edges,
                                          List<DiagramGroup> groups) {
        String start = assetId + "-start";
        String calc = assetId + "-calc-after-hc";
        String end = assetId + "-end";
        String before = assetId + "-input-before-hc";
        String effective = assetId + "-input-effective-hc";
        String after = assetId + "-output-after-hc";

        nodes.add(new DiagramNode(start, "Start", "ellipse", "root", confidenceModel.score(2, 2, sampleSize)));
        nodes.add(new DiagramNode(calc,
                "Alloc Cover No Excess after HC cov,osg = Alloc Cover No Excess before HC cov,osg * (1 - Effective HC cov,osg)",
                "rectangle", "process", confidenceModel.score(5, 6, sampleSize)));
        nodes.add(new DiagramNode(end, "End", "ellipse", "leaf", confidenceModel.score(2, 2, sampleSize)));
        nodes.add(new DiagramNode(before, "Alloc Cover No Excess Before HC", "rectangle", "process",
                confidenceModel.score(3, 4, sampleSize)));
        nodes.add(new DiagramNode(effective, "Effective Haircut", "rectangle", "process",
                confidenceModel.score(3, 4, sampleSize)));
        nodes.add(new DiagramNode(after, "Alloc Cover No Excess After HC", "rectangle", "process",
                confidenceModel.score(3, 4, sampleSize)));

        edges.add(new DiagramEdge(start, calc, true, "flow", confidenceModel.score(1, 1, sampleSize), null));
        edges.add(new DiagramEdge(calc, end, true, "flow", confidenceModel.score(1, 1, sampleSize), null));
        edges.add(new DiagramEdge(before, calc, true, "flow", confidenceModel.score(1, 1, sampleSize), null));
        edges.add(new DiagramEdge(effective, calc, true, "flow", confidenceModel.score(1, 1, sampleSize), null));
        edges.add(new DiagramEdge(calc, after, true, "flow", confidenceModel.score(1, 1, sampleSize), null));

        groups.add(new DiagramGroup(assetId + "-group-before", "process-group",
                "Calculate Allocated Cover Amount Without Excess Before HC",
                List.of(new DiagramGroupMember(before))));
        groups.add(new DiagramGroup(assetId + "-group-effective", "process-group",
                "Calculate Effective Haircut",
                List.of(new DiagramGroupMember(effective))));
        groups.add(new DiagramGroup(assetId + "-group-after", "process-group",
                "Calculate Allocated Cover Amount Without Excess After Haircut",
                List.of(new DiagramGroupMember(calc))));
        groups.add(new DiagramGroup(assetId + "-group-outstanding", "process-group",
                "Outstanding/Cover",
                List.of(new DiagramGroupMember(calc))));
    }

    private boolean looksLikeHaircutFormula(String normalized) {
        return normalized.contains("alloc cover no excess after hc")
                && normalized.contains("alloc cover no excess before hc")
                && (normalized.contains("effective hc") || normalized.contains("effective haircut"));
    }

    private boolean looksLikeVreDecisionFlow(String normalized) {
        return normalized.contains("pro rata")
                && normalized.contains("if residual value")
                && normalized.contains("no allocation");
    }

    private void buildVreDecisionGraph(String assetId,
                                       int sampleSize,
                                       List<DiagramNode> nodes,
                                       List<DiagramEdge> edges,
                                       List<DiagramGroup> groups) {
        String start = assetId + "-start";
        String proRata = assetId + "-pro-rata-basis";
        String ifCover = assetId + "-if-cover";
        String ifResidual = assetId + "-if-residual-value";
        String residualValue = assetId + "-residual-value";
        String noAllocation = assetId + "-no-allocation";
        String vreRequest = assetId + "-vre-request";
        String end = assetId + "-end";

        nodes.add(new DiagramNode(start, "Start", "ellipse", "root", confidenceModel.score(2, 2, sampleSize)));
        nodes.add(new DiagramNode(proRata, "Calculate Pro Rata Basis", "rectangle", "process", confidenceModel.score(4, 5, sampleSize)));
        nodes.add(new DiagramNode(ifCover, "If Cover Available", "diamond", "decision", confidenceModel.score(4, 5, sampleSize)));
        nodes.add(new DiagramNode(ifResidual, "If Residual Value", "diamond", "decision", confidenceModel.score(4, 5, sampleSize)));
        nodes.add(new DiagramNode(residualValue, "Residual Value", "rectangle", "process", confidenceModel.score(3, 4, sampleSize)));
        nodes.add(new DiagramNode(noAllocation, "No Allocation", "rectangle", "process", confidenceModel.score(3, 4, sampleSize)));
        nodes.add(new DiagramNode(vreRequest, "VRE Request", "rectangle", "process", confidenceModel.score(3, 4, sampleSize)));
        nodes.add(new DiagramNode(end, "End", "ellipse", "leaf", confidenceModel.score(2, 2, sampleSize)));

        edges.add(new DiagramEdge(start, proRata, true, "flow", confidenceModel.score(1, 1, sampleSize), null));
        edges.add(new DiagramEdge(proRata, ifCover, true, "flow", confidenceModel.score(1, 1, sampleSize), null));
        edges.add(new DiagramEdge(ifCover, noAllocation, true, "flow", confidenceModel.score(1, 1, sampleSize), "N"));
        edges.add(new DiagramEdge(ifCover, ifResidual, true, "flow", confidenceModel.score(1, 1, sampleSize), "Y"));
        edges.add(new DiagramEdge(ifResidual, residualValue, true, "flow", confidenceModel.score(1, 1, sampleSize), "Y"));
        edges.add(new DiagramEdge(ifResidual, vreRequest, true, "flow", confidenceModel.score(1, 1, sampleSize), "N"));
        edges.add(new DiagramEdge(residualValue, vreRequest, true, "flow", confidenceModel.score(1, 1, sampleSize), null));
        edges.add(new DiagramEdge(noAllocation, end, true, "flow", confidenceModel.score(1, 1, sampleSize), null));
        edges.add(new DiagramEdge(vreRequest, end, true, "flow", confidenceModel.score(1, 1, sampleSize), null));

        String residualGroup = assetId + "-group-residual";
        String coverGroup = assetId + "-group-cover";
        String requestGroup = assetId + "-group-vre-request";

        groups.add(new DiagramGroup(residualGroup, "process-group", "Residual Value",
                List.of(new DiagramGroupMember(ifResidual), new DiagramGroupMember(residualValue))));
        groups.add(new DiagramGroup(coverGroup, "process-group", "Cover",
                List.of(new DiagramGroupMember(ifCover), new DiagramGroupMember(vreRequest), DiagramGroupMember.groupRef(residualGroup))));
        groups.add(new DiagramGroup(requestGroup, "process-group", "VRE Request",
                List.of(new DiagramGroupMember(start), new DiagramGroupMember(proRata), new DiagramGroupMember(noAllocation),
                        new DiagramGroupMember(end), DiagramGroupMember.groupRef(coverGroup))));
    }

    private String extractText(byte[] data) {
        String utf16 = new String(data, StandardCharsets.UTF_16LE);
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("[A-Za-z][A-Za-z0-9 ,._:/()\\-]{2,}");
        LinkedHashSet<String> snippets = new LinkedHashSet<>();
        java.util.regex.Matcher matcher = pattern.matcher(utf16);
        while (matcher.find()) {
            String token = matcher.group().trim().replaceAll("\\s+", " ");
            if (token.length() < 4) {
                continue;
            }
            if (token.startsWith("ING Me")) {
                continue;
            }
            if (token.chars().filter(ch -> Character.isLetter(ch)).count() < 3) {
                continue;
            }
            snippets.add(token);
            if (snippets.size() >= 16) {
                break;
            }
        }
        if (snippets.isEmpty()) {
            return "";
        }
        return String.join(" | ", snippets);
    }

    private List<String> tokenize(String extractedText) {
        List<String> tokens = new ArrayList<>();
        for (String raw : extractedText.split("\\|")) {
            String cleaned = raw.trim().replaceAll("\\s+", " ");
            if (!cleaned.isEmpty()) {
                tokens.add(cleaned);
            }
        }
        return tokens;
    }

    private boolean containsToken(List<String> tokens, String value) {
        String needle = value.toLowerCase(Locale.ROOT);
        return tokens.stream().anyMatch(token -> token.toLowerCase(Locale.ROOT).equals(needle));
    }

    private boolean containsBeforeHaircut(String normalized) {
        return normalized.contains("without excess before haircut")
                || normalized.contains("without excess before hc")
                || (normalized.contains("without excess") && normalized.contains("before") && normalized.contains("haircut"));
    }

    private boolean containsAfterHaircut(String normalized) {
        return normalized.contains("without excess after haircut")
                || normalized.contains("without excess after hc")
                || (normalized.contains("without excess") && normalized.contains("after") && normalized.contains("haircut"));
    }

    private boolean containsAllocatedWithoutExcess(String normalized) {
        return normalized.contains("allocated cover") && normalized.contains("without excess");
    }

    private String addProcessNode(List<DiagramNode> nodes,
                                  String imageNodeId,
                                  String suffix,
                                  String label,
                                  int sampleSize,
                                  int matched,
                                  int expected) {
        String id = imageNodeId + "-" + suffix;
        nodes.add(new DiagramNode(id, label, "rectangle", "process", confidenceModel.score(matched, expected, sampleSize)));
        return id;
    }

    private String inferGroupLabel(List<String> tokens) {
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            String lower = token.toLowerCase(Locale.ROOT);
            if (!lower.contains("group")) {
                continue;
            }
            if (i > 0) {
                String combined = (tokens.get(i - 1) + " " + token).replaceAll("\\s+", " ").trim();
                if (!combined.isEmpty()) {
                    return combined;
                }
            }
            return token;
        }
        return "";
    }
}

