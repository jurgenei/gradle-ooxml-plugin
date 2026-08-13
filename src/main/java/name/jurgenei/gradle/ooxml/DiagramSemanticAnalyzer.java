package name.jurgenei.gradle.ooxml;

import name.jurgenei.gradle.ooxml.canonical.Diagram;
import name.jurgenei.gradle.ooxml.canonical.DiagramNode;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Two-pass semantic inference over canonical diagram topology.
 */
final class DiagramSemanticAnalyzer {
    private static final double ROOT_LEAF_CONFIDENCE = 0.70;
    private static final double DECISION_CONFIDENCE = 0.80;

    void infer(Diagram diagram) {
        Map<String, DiagramNode> byId = diagram.getNodes().stream()
                .filter(node -> node.getId() != null && !node.getId().isBlank())
                .collect(Collectors.toMap(DiagramNode::getId, Function.identity(), (left, right) -> left));

        DefaultDirectedGraph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        byId.keySet().forEach(graph::addVertex);
        diagram.getEdges().forEach(edge -> {
            String source = edge.getSource();
            String target = edge.getTarget();
            if (source == null || target == null || source.isBlank() || target.isBlank()) {
                return;
            }
            graph.addVertex(source);
            graph.addVertex(target);
            if (!graph.containsEdge(source, target)) {
                graph.addEdge(source, target);
            }
        });

        for (String vertex : graph.vertexSet()) {
            DiagramNode node = byId.get(vertex);
            if (node == null || node.getSemantic() != null) {
                continue;
            }
            int in = graph.inDegreeOf(vertex);
            int out = graph.outDegreeOf(vertex);

            if (out > 1 && "diamond".equalsIgnoreCase(node.getGeometry())) {
                node.setSemantic("decision");
                node.setConfidence(DECISION_CONFIDENCE);
                continue;
            }
            if (in == 0 && out > 0) {
                node.setSemantic("root");
                node.setConfidence(ROOT_LEAF_CONFIDENCE);
                continue;
            }
            if (out == 0 && in > 0) {
                node.setSemantic("leaf");
                node.setConfidence(ROOT_LEAF_CONFIDENCE);
            }
        }
    }
}

