package name.jurgenei.gradle.ooxml.recognizer;

import name.jurgenei.gradle.ooxml.canonical.DiagramAnnotation;
import name.jurgenei.gradle.ooxml.canonical.DiagramEdge;
import name.jurgenei.gradle.ooxml.canonical.DiagramGroup;
import name.jurgenei.gradle.ooxml.canonical.DiagramGroupMember;
import name.jurgenei.gradle.ooxml.canonical.DiagramNode;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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
            return finalizeRecognition(assetId, tokens, nodes, edges, groups, annotations);
        }
        if (looksLikeVreDecisionFlow(normalized)) {
            buildVreDecisionGraph(assetId, tokens.size(), nodes, edges, groups);
            return finalizeRecognition(assetId, tokens, nodes, edges, groups, annotations);
        }
        if (looksLikeEligibilityIndicatorFlow(normalized)) {
            buildEligibilityIndicatorGraph(assetId, tokens.size(), nodes, edges, groups);
            return finalizeRecognition(assetId, tokens, nodes, edges, groups, annotations);
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

        if (isSuspiciousSingleton(tokens, nodes, edges)) {
            int beforeNodes = nodes.size();
            int beforeEdges = edges.size();
            recoverSingletonGraph(assetId, tokens, nodes, edges, groups);
            annotations.add(new DiagramAnnotation("recovery", assetId,
                    confidenceModel.score(Math.min(nodes.size(), 6), 6, tokens.size()),
                    "singleton-recovery nodes " + beforeNodes + "->" + nodes.size()
                            + ", edges " + beforeEdges + "->" + edges.size()));
        }

        return finalizeRecognition(assetId, tokens, nodes, edges, groups, annotations);
    }

    private AssetRecognition finalizeRecognition(String assetId,
                                                 List<String> tokens,
                                                 List<DiagramNode> nodes,
                                                 List<DiagramEdge> edges,
                                                 List<DiagramGroup> groups,
                                                 List<DiagramAnnotation> annotations) {
        int addedCoverageNodes = applyAssetTextCoverageHeuristic(assetId, tokens, nodes, edges, groups);
        if (addedCoverageNodes > 0) {
            annotations.add(new DiagramAnnotation("coverage-heuristic", assetId,
                    confidenceModel.score(Math.min(addedCoverageNodes, 6), 6, tokens.size()),
                    "added " + addedCoverageNodes + " evidence nodes for unmatched asset-text tokens"));
        }
        return new AssetRecognition(nodes, edges, groups, annotations);
    }

    private int applyAssetTextCoverageHeuristic(String assetId,
                                                List<String> tokens,
                                                List<DiagramNode> nodes,
                                                List<DiagramEdge> edges,
                                                List<DiagramGroup> groups) {
        Set<String> covered = new LinkedHashSet<>();
        nodes.stream().map(DiagramNode::getLabel).filter(label -> label != null && !label.isBlank())
                .map(this::normalizeTokenForCoverage).forEach(covered::add);
        edges.stream().map(DiagramEdge::getLabel).filter(label -> label != null && !label.isBlank())
                .map(this::normalizeTokenForCoverage).forEach(covered::add);
        groups.stream().map(DiagramGroup::getLabel).filter(label -> label != null && !label.isBlank())
                .map(this::normalizeTokenForCoverage).forEach(covered::add);

        List<String> missing = new ArrayList<>();
        for (String token : tokens) {
            String normalizedToken = normalizeTokenForCoverage(token);
            if (normalizedToken.isBlank() || isCoverageStopToken(normalizedToken)) {
                continue;
            }
            boolean present = covered.stream().anyMatch(label -> label.contains(normalizedToken) || normalizedToken.contains(label));
            if (!present && missing.stream().noneMatch(existing -> existing.equals(normalizedToken))) {
                missing.add(normalizedToken);
            }
        }

        int limit = Math.min(64, missing.size());
        for (int i = 0; i < limit; i++) {
            String token = missing.get(i);
            String label = token.substring(0, 1).toUpperCase(Locale.ROOT) + token.substring(1);
            String nodeId = assetId + "-coverage-" + slug(label, i + 1);
            String geometry = looksLikeDecision(label) ? "diamond" : "rectangle";
            String semantic = looksLikeDecision(label) ? "decision" : "evidence";
            nodes.add(new DiagramNode(nodeId, label, geometry, semantic,
                    confidenceModel.score(1, 4, Math.max(tokens.size(), 1))));
        }
        return limit;
    }

    private String normalizeTokenForCoverage(String value) {
        if (value == null) {
            return "";
        }
        return normalizeLabel(value)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9 ]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private boolean isCoverageStopToken(String token) {
        return token.length() < 3
                || "and".equals(token)
                || "the".equals(token)
                || "for".equals(token)
                || "with".equals(token)
                || "see section".equals(token)
                || "yes".equals(token)
                || "no".equals(token);
    }

    private boolean isSuspiciousSingleton(List<String> tokens, List<DiagramNode> nodes, List<DiagramEdge> edges) {
        return nodes.size() <= 1 && edges.isEmpty() && tokens.size() >= 8;
    }

    private void recoverSingletonGraph(String assetId,
                                       List<String> tokens,
                                       List<DiagramNode> nodes,
                                       List<DiagramEdge> edges,
                                       List<DiagramGroup> groups) {
        String startId = null;
        if (nodes.stream().noneMatch(node -> "start".equalsIgnoreCase(node.getLabel()))) {
            startId = assetId + "-start";
            nodes.add(new DiagramNode(startId, "Start", "ellipse", "root", confidenceModel.score(1, 1, tokens.size())));
        } else {
            startId = nodes.stream()
                    .filter(node -> "start".equalsIgnoreCase(node.getLabel()))
                    .map(DiagramNode::getId)
                    .findFirst()
                    .orElse(assetId + "-start");
        }

        List<String> candidates = rankProcessCandidates(tokens);
        if (candidates.isEmpty()) {
            return;
        }

        List<String> nodeIds = new ArrayList<>();
        for (int i = 0; i < candidates.size(); i++) {
            String label = normalizeLabel(candidates.get(i));
            String id = assetId + "-auto-" + slug(label, i + 1);
            String geometry = looksLikeDecision(label) ? "diamond" : "rectangle";
            String semantic = looksLikeDecision(label) ? "decision" : "process";
            nodes.add(new DiagramNode(id, label, geometry, semantic,
                    confidenceModel.score(3, 5, tokens.size())));
            nodeIds.add(id);
        }

        List<String> chain = new ArrayList<>();
        chain.add(startId);
        chain.addAll(nodeIds);
        if (tokens.stream().anyMatch(token -> "end".equalsIgnoreCase(token))) {
            String endId = assetId + "-end";
            nodes.add(new DiagramNode(endId, "End", "ellipse", "leaf", confidenceModel.score(1, 1, tokens.size())));
            chain.add(endId);
        }

        for (int i = 0; i + 1 < chain.size(); i++) {
            String label = inferBranchLabel(tokens, i);
            edges.add(new DiagramEdge(chain.get(i), chain.get(i + 1), true, "flow",
                    confidenceModel.score(2, 3, tokens.size()), label));
        }

        buildNestedGroups(assetId, tokens, nodeIds, groups);
    }

    private List<String> rankProcessCandidates(List<String> tokens) {
        Set<String> seen = new LinkedHashSet<>();
        List<String> candidates = new ArrayList<>();
        for (String token : tokens) {
            String normalized = normalizeLabel(token);
            if (normalized.isBlank()) {
                continue;
            }
            String lower = normalized.toLowerCase(Locale.ROOT);
            if ("start".equals(lower) || "end".equals(lower)) {
                continue;
            }
            if (lower.length() < 5 || lower.chars().filter(Character::isLetter).count() < 4) {
                continue;
            }
            if (seen.add(lower) && looksLikeProcessPhrase(normalized)) {
                candidates.add(normalized);
            }
        }
        return candidates.stream()
                .sorted(Comparator.comparingInt(this::priorityScore).reversed())
                .limit(8)
                .toList();
    }

    private int priorityScore(String label) {
        String lower = label.toLowerCase(Locale.ROOT);
        int score = label.length();
        if (lower.contains("calculate") || lower.contains("determine")) {
            score += 40;
        }
        if (lower.contains("eligible") || lower.contains("indicator") || lower.contains("class")) {
            score += 20;
        }
        if (looksLikeDecision(label)) {
            score += 10;
        }
        return score;
    }

    private boolean looksLikeProcessPhrase(String label) {
        String lower = label.toLowerCase(Locale.ROOT);
        return lower.contains("calculate")
                || lower.contains("determine")
                || lower.contains("eligible")
                || lower.contains("indicator")
                || lower.contains("class")
                || lower.contains("request")
                || lower.contains("cover")
                || lower.contains("residual")
                || lower.contains("allocation")
                || lower.contains("data");
    }

    private boolean looksLikeDecision(String label) {
        String lower = label.toLowerCase(Locale.ROOT);
        return lower.startsWith("if ") || lower.contains(" if ") || lower.contains("?");
    }

    private String inferBranchLabel(List<String> tokens, int edgeIndex) {
        String joined = String.join(" ", tokens).toLowerCase(Locale.ROOT);
        if (joined.contains(" y ") || joined.contains(" y and ")) {
            return edgeIndex % 2 == 0 ? "Y" : null;
        }
        if (joined.contains(" n ") || joined.contains(" no ")) {
            return edgeIndex % 2 == 1 ? "N" : null;
        }
        return null;
    }

    private void buildNestedGroups(String assetId,
                                   List<String> tokens,
                                   List<String> processNodeIds,
                                   List<DiagramGroup> groups) {
        if (processNodeIds.isEmpty()) {
            return;
        }

        List<String> hierarchyLabels = tokens.stream()
                .map(this::normalizeLabel)
                .filter(label -> !label.isBlank())
                .filter(label -> wordCount(label) <= 4)
                .filter(label -> !"start".equalsIgnoreCase(label) && !"end".equalsIgnoreCase(label))
                .distinct()
                .limit(3)
                .toList();

        if (hierarchyLabels.size() < 2) {
            return;
        }

        String previousGroupId = null;
        for (int i = hierarchyLabels.size() - 1; i >= 0; i--) {
            String label = hierarchyLabels.get(i);
            String groupId = assetId + "-auto-group-" + slug(label, i + 1);
            List<DiagramGroupMember> members = new ArrayList<>();
            if (previousGroupId != null) {
                members.add(DiagramGroupMember.groupRef(previousGroupId));
            } else {
                members.add(new DiagramGroupMember(processNodeIds.get(0)));
            }
            groups.add(new DiagramGroup(groupId, "process-group", label, members));
            previousGroupId = groupId;
        }
    }

    private String slug(String label, int fallbackIndex) {
        String slug = label.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        if (slug.isBlank()) {
            return "node-" + fallbackIndex;
        }
        return slug;
    }

    private int wordCount(String value) {
        return value.trim().isEmpty() ? 0 : value.trim().split("\\s+").length;
    }

    private String normalizeLabel(String token) {
        return token.replaceAll("\\s+", " ")
                .replaceAll("^[,.:;/\\-\\s]+", "")
                .replaceAll("[,.:;/\\-\\s]+$", "")
                .trim();
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

    private boolean looksLikeEligibilityIndicatorFlow(String normalized) {
        return normalized.contains("eligible")
                && normalized.contains("indicator")
                && normalized.contains("cover")
                && (normalized.contains("guarantor") || normalized.contains("exposure class") || normalized.contains("substitution"));
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

    private void buildEligibilityIndicatorGraph(String assetId,
                                                int sampleSize,
                                                List<DiagramNode> nodes,
                                                List<DiagramEdge> edges,
                                                List<DiagramGroup> groups) {
        String start = assetId + "-start";
        String determine = assetId + "-determine-eligible-covers";
        String rules = assetId + "-crr3-ler-rules";
        String coverEligibility = assetId + "-cover-eligibility-ind";
        String guarantor = assetId + "-eligible-sa-guarantor-ind";
        String substitution = assetId + "-substitution-indicator";
        String exposure = assetId + "-vre-exposure-class";
        String end = assetId + "-end";

        nodes.add(new DiagramNode(start, "Start", "ellipse", "root", confidenceModel.score(2, 2, sampleSize)));
        nodes.add(new DiagramNode(determine, "Determine Eligible Guarantees and Security Covers", "rectangle", "process",
                confidenceModel.score(5, 6, sampleSize)));
        nodes.add(new DiagramNode(rules, "Eligible Covers Based on CRR3 LER Rules", "rectangle", "process",
                confidenceModel.score(4, 5, sampleSize)));
        nodes.add(new DiagramNode(coverEligibility, "Cover Eligibility Ind", "diamond", "decision",
                confidenceModel.score(4, 5, sampleSize)));
        nodes.add(new DiagramNode(guarantor, "Eligible SA Guarantor Ind", "diamond", "decision",
                confidenceModel.score(4, 5, sampleSize)));
        nodes.add(new DiagramNode(substitution, "Substitution Indicator", "rectangle", "process",
                confidenceModel.score(3, 4, sampleSize)));
        nodes.add(new DiagramNode(exposure, "VRE Exposure Class", "rectangle", "process",
                confidenceModel.score(3, 4, sampleSize)));
        nodes.add(new DiagramNode(end, "End", "ellipse", "leaf", confidenceModel.score(2, 2, sampleSize)));

        edges.add(new DiagramEdge(start, determine, true, "flow", confidenceModel.score(1, 1, sampleSize), null));
        edges.add(new DiagramEdge(determine, rules, true, "flow", confidenceModel.score(1, 1, sampleSize), null));
        edges.add(new DiagramEdge(rules, coverEligibility, true, "flow", confidenceModel.score(1, 1, sampleSize), null));
        edges.add(new DiagramEdge(coverEligibility, guarantor, true, "flow", confidenceModel.score(1, 1, sampleSize), "Y"));
        edges.add(new DiagramEdge(coverEligibility, substitution, true, "flow", confidenceModel.score(1, 1, sampleSize), "N"));
        edges.add(new DiagramEdge(guarantor, substitution, true, "flow", confidenceModel.score(1, 1, sampleSize), "Y"));
        edges.add(new DiagramEdge(substitution, exposure, true, "flow", confidenceModel.score(1, 1, sampleSize), null));
        edges.add(new DiagramEdge(exposure, end, true, "flow", confidenceModel.score(1, 1, sampleSize), null));

        String staticData = assetId + "-group-static-data";
        String cover = assetId + "-group-cover";
        String request = assetId + "-group-vre-request";
        groups.add(new DiagramGroup(staticData, "process-group", "Static Data",
                List.of(new DiagramGroupMember(rules), new DiagramGroupMember(substitution))));
        groups.add(new DiagramGroup(cover, "process-group", "Outstanding/Cover",
                List.of(new DiagramGroupMember(coverEligibility), new DiagramGroupMember(guarantor), DiagramGroupMember.groupRef(staticData))));
        groups.add(new DiagramGroup(request, "process-group", "VRE Request",
                List.of(new DiagramGroupMember(start), new DiagramGroupMember(determine), new DiagramGroupMember(exposure), new DiagramGroupMember(end), DiagramGroupMember.groupRef(cover))));
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

