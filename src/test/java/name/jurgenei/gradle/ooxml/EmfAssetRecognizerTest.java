package name.jurgenei.gradle.ooxml;

import name.jurgenei.gradle.ooxml.canonical.CanonicalDocument;
import name.jurgenei.gradle.ooxml.canonical.Diagram;
import name.jurgenei.gradle.ooxml.canonical.DiagramEdge;
import name.jurgenei.gradle.ooxml.canonical.DiagramNode;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmfAssetRecognizerTest {
    private final OoXmlCanonicalizer canonicalizer = new OoXmlCanonicalizer();

    @Test
    void recognizesExpectedTopologyForV2DiagramFixture() throws Exception {
        Path file = copyFixture("v2-diagrams.docx");
        CanonicalDocument document = canonicalizer.canonicalize(file.toFile());

        Diagram firstDiagram = diagramBySource(document, "/word/document/p[2]/drawing[1]");
        Diagram secondDiagram = diagramBySource(document, "/word/document/p[3]/drawing[1]");
        Diagram thirdDiagram = diagramBySource(document, "/word/document/p[5]/drawing[1]");

        assertContainsNodeLabel(firstDiagram, "Calculate Uncovered Amount (see section a)");
        assertContainsNodeLabel(firstDiagram, "Calculate Allocated Cover Amount without excess before haircut (see section b)");
        assertContainsNodeLabel(firstDiagram, "Calculate Allocated Cover Amount without excess after haircut (see section c)");
        assertTrue(firstDiagram.getEdges().size() >= 3, "First diagram should have at least a 3-step flow.");
        assertTrue(firstDiagram.getGroups().stream().anyMatch(group ->
                group.getLabel() != null
                        && group.getLabel().contains("Outstanding Group")
                        && group.getMembers().size() >= 2));

        assertContainsNodeLabel(secondDiagram, "Start");
        assertContainsNodeLabel(secondDiagram, "End");
        assertContainsNodeLabel(secondDiagram, "Alloc Cover No Excess Before HC");
        assertContainsNodeLabel(secondDiagram, "Effective Haircut");
        assertContainsNodeLabel(secondDiagram, "Alloc Cover No Excess After HC");
        assertContainsNodeLabel(secondDiagram, "Alloc Cover No Excess after HC cov,osg = Alloc Cover No Excess before HC cov,osg * (1 - Effective HC cov,osg)");

        Map<String, String> labelToId = secondDiagram.getNodes().stream()
                .collect(Collectors.toMap(DiagramNode::getLabel, DiagramNode::getId, (left, right) -> left));

        String calc = labelToId.get("Alloc Cover No Excess after HC cov,osg = Alloc Cover No Excess before HC cov,osg * (1 - Effective HC cov,osg)");
        assertNotNull(calc);
        assertHasEdge(secondDiagram, labelToId.get("Alloc Cover No Excess Before HC"), calc);
        assertHasEdge(secondDiagram, labelToId.get("Effective Haircut"), calc);

        assertTrue(secondDiagram.getAnnotations().stream().anyMatch(a -> "emf-stats".equals(a.getKind())));

        assertContainsNodeLabel(thirdDiagram, "Start");
        assertContainsNodeLabel(thirdDiagram, "If Residual Value");
        assertContainsNodeLabel(thirdDiagram, "No Allocation");
        assertTrue(thirdDiagram.getNodes().stream().filter(node -> "diamond".equals(node.getGeometry())).count() >= 2,
                "Third diagram should include two decision diamonds.");
        assertTrue(thirdDiagram.getEdges().stream().anyMatch(edge -> "Y".equals(edge.getLabel())),
                "Third diagram should include Y decision edge labels.");
        assertTrue(thirdDiagram.getEdges().stream().anyMatch(edge -> "N".equals(edge.getLabel())),
                "Third diagram should include N decision edge labels.");
        assertTrue(thirdDiagram.getGroups().stream().anyMatch(group -> "Residual Value".equals(group.getLabel())));
        assertTrue(thirdDiagram.getGroups().stream().anyMatch(group -> "Cover".equals(group.getLabel())));
        assertTrue(thirdDiagram.getGroups().stream().anyMatch(group -> "VRE Request".equals(group.getLabel())));
        assertTrue(thirdDiagram.getGroups().stream().anyMatch(group -> group.getMembers().stream().anyMatch(member -> member.getGroup() != null)),
                "Third diagram should contain nested group references.");

        assertConfidenceBounds(firstDiagram.getNodes(), firstDiagram.getEdges());
        assertConfidenceBounds(secondDiagram.getNodes(), secondDiagram.getEdges());
        assertConfidenceBounds(thirdDiagram.getNodes(), thirdDiagram.getEdges());
    }

    private void assertConfidenceBounds(List<DiagramNode> nodes, List<DiagramEdge> edges) {
        assertTrue(nodes.stream().allMatch(node -> node.getConfidence() != null && node.getConfidence() >= 0.40 && node.getConfidence() <= 0.97));
        assertTrue(edges.stream().allMatch(edge -> edge.getConfidence() != null && edge.getConfidence() >= 0.40 && edge.getConfidence() <= 0.97));
    }

    private void assertHasEdge(Diagram diagram, String source, String target) {
        assertNotNull(source);
        assertNotNull(target);
        assertTrue(diagram.getEdges().stream().anyMatch(edge -> source.equals(edge.getSource()) && target.equals(edge.getTarget())));
    }

    private void assertContainsNodeLabel(Diagram diagram, String label) {
        assertTrue(diagram.getNodes().stream().anyMatch(node -> label.equals(node.getLabel())));
    }

    private Diagram diagramBySource(CanonicalDocument document, String sourcePath) {
        return document.getBody().getDiagrams().stream()
                .filter(diagram -> sourcePath.equals(diagram.getSourcePath()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing diagram for source path: " + sourcePath));
    }

    private Path copyFixture(String fixtureName) throws Exception {
        Path dir = Files.createTempDirectory("ooxml-fixture-");
        Path temp = dir.resolve(fixtureName);
        try (InputStream input = getClass().getResourceAsStream("/ooxml/" + fixtureName)) {
            if (input == null) {
                throw new IllegalStateException("Missing fixture: " + fixtureName);
            }
            Files.copy(input, temp, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        return temp;
    }
}

