package name.jurgenei.gradle.ooxml;

import name.jurgenei.gradle.ooxml.canonical.CanonicalDocument;
import org.w3c.dom.Element;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OoXmlCanonicalizerTest {
    private final OoXmlCanonicalizer canonicalizer = new OoXmlCanonicalizer();
    private final CanonicalXmlSerializer serializer = new CanonicalXmlSerializer();

    @Test
    void canonicalizesDocxBenchmark() throws Exception {
        Path file = copyFixture("v1-benchmark.docx");

        CanonicalDocument document = canonicalizer.canonicalize(file.toFile());

        assertEquals("v1-benchmark", document.getMetadata().getDocumentId());
        assertEquals("", document.getMetadata().getVersion());
        assertEquals("DOCX", document.getMetadata().getDocumentType());
        assertFalse(document.getBody().getParagraphs().isEmpty());
        assertTrue(document.getBody().getParagraphs().stream().anyMatch(p -> p.getText().contains("Benchmark Document")));
        assertTrue(document.getBody().getParagraphs().stream().anyMatch(p -> "Benchmark Document".equals(p.getText()) && "h1".equals(p.getLabel())));
        assertTrue(document.getBody().getParagraphs().stream().anyMatch(p -> "Section A".equals(p.getText()) && "h2".equals(p.getLabel())));
        assertTrue(document.getBody().getParagraphs().stream().anyMatch(p -> "Section B".equals(p.getText()) && "h2".equals(p.getLabel())));
        assertTrue(document.getBody().getParagraphs().stream().anyMatch(p -> p.getText().contains("Section A")));
        assertTrue(document.getBody().getParagraphs().stream().anyMatch(p -> p.getText().contains("Section B")));
        assertTrue(document.getBody().getParagraphs().stream().anyMatch(p -> p.getText().contains("Paragraph with bold")));
        assertTrue(document.getBody().getParagraphs().stream().anyMatch(p -> p.getText().contains("Visit https://example.com")));
        assertTrue(document.getBody().getParagraphs().stream().anyMatch(p -> p.getText().contains("[A] -> [B]")));
        assertTrue(document.getBody().getParagraphs().stream().anyMatch(p -> p.getText().contains("Final paragraph")));
        List<String> paragraphTexts = document.getBody().getParagraphs().stream().map(p -> p.getText()).toList();
        int sectionA = paragraphTexts.indexOf("Section A");
        int diagram = paragraphTexts.indexOf("[A] -> [B]");
        int sectionB = paragraphTexts.indexOf("Section B");
        int finalParagraph = paragraphTexts.indexOf("Final paragraph");
        assertTrue(sectionA >= 0 && diagram > sectionA && sectionB > diagram && finalParagraph > sectionB,
                "DOCX paragraph order should follow source order");
        assertTrue(document.getBody().getParagraphs().stream().anyMatch(p -> p.getSourcePath() != null && p.getSourcePath().startsWith("/word/document/")));
        assertEquals(2, document.getBody().getLists().size());
        assertTrue(document.getBody().getLists().stream().anyMatch(list -> list.isOrdered() && list.getItems().stream().anyMatch(item -> item.getText().contains("First item"))));
        assertTrue(document.getBody().getLists().stream().anyMatch(list -> !list.isOrdered() && list.getItems().stream().anyMatch(item -> item.getText().contains("Alpha"))));
        assertFalse(document.getBody().getTables().isEmpty());
        assertTrue(document.getBody().getTables().stream().anyMatch(table -> table.getRows().stream().flatMap(row -> row.getCells().stream()).anyMatch(cell -> cell.getText().contains("App"))));
        assertTrue(document.getBody().getTables().stream().anyMatch(table -> table.getRows().stream().flatMap(row -> row.getCells().stream()).anyMatch(cell -> cell.getText().contains("Sales"))));
        assertEquals(1, document.getBody().getLinks().size());
        assertTrue(document.getBody().getLinks().stream().anyMatch(link -> "https://example.com".equals(link.getTarget()) && "https://example.com".equals(link.getText())));
    }

    @Test
    void canonicalizesPptxBenchmark() throws Exception {
        Path file = copyFixture("v1-benchmark.pptx");

        CanonicalDocument document = canonicalizer.canonicalize(file.toFile());

        assertEquals("PPTX", document.getMetadata().getDocumentType());
        assertTrue(document.getBody().getParagraphs().stream().anyMatch(p -> p.getText().contains("Overview")));
        assertTrue(document.getBody().getParagraphs().stream().anyMatch(p -> p.getText().contains("System landscape overview")));
        assertTrue(document.getBody().getParagraphs().stream().anyMatch(p -> p.getText().contains("Architecture")));
        assertTrue(document.getBody().getParagraphs().stream().anyMatch(p -> p.getText().contains("Responsibilities")));
        assertTrue(document.getBody().getParagraphs().stream().anyMatch(p -> p.getSourcePath() != null && p.getSourcePath().startsWith("/ppt/slides/")));
        assertTrue(document.getBody().getLists().isEmpty());
        assertFalse(document.getBody().getTables().isEmpty());
        assertTrue(document.getBody().getTables().stream().anyMatch(table -> table.getRows().stream().flatMap(row -> row.getCells().stream()).anyMatch(cell -> cell.getText().contains("App"))));
        assertTrue(document.getBody().getTables().stream().anyMatch(table -> table.getSourcePath() != null && table.getSourcePath().startsWith("/ppt/slides/")));
        assertFalse(document.getBody().getDiagrams().isEmpty());
        assertTrue(document.getBody().getDiagrams().stream().anyMatch(diagram -> diagram.getShapes().stream().anyMatch(shape -> "CRM".equals(shape.getLabel()))));
        assertTrue(document.getBody().getDiagrams().stream().anyMatch(diagram -> diagram.getShapes().stream().anyMatch(shape -> "SAP".equals(shape.getLabel()))));
        assertTrue(document.getBody().getDiagrams().stream().anyMatch(diagram -> diagram.getSourcePath() != null && diagram.getSourcePath().startsWith("/ppt/slides/")));
        assertTrue(document.getBody().getDiagrams().stream().anyMatch(diagram -> diagram.getConnectors().stream().anyMatch(connector -> "3".equals(connector.getSource()) && "4".equals(connector.getTarget()))));
    }

    @Test
    void canonicalizesXlsxBenchmark() throws Exception {
        Path file = copyFixture("v1-benchmark.xlsx");

        CanonicalDocument document = canonicalizer.canonicalize(file.toFile());

        assertEquals("XLSX", document.getMetadata().getDocumentType());
        assertTrue(document.getBody().getParagraphs().isEmpty());
        assertTrue(document.getBody().getTables().size() >= 3);
        assertTrue(document.getBody().getTables().stream().anyMatch(table -> "Applications".equals(table.getId())));
        assertTrue(document.getBody().getTables().stream().anyMatch(table -> "Matrix".equals(table.getId())));
        assertTrue(document.getBody().getTables().stream().anyMatch(table -> "NamedRange".equals(table.getId())));
        assertTrue(document.getBody().getTables().stream().anyMatch(table -> table.getRows().stream().flatMap(row -> row.getCells().stream()).anyMatch(cell -> cell.getText().contains("Application"))));
        assertTrue(document.getBody().getTables().stream().anyMatch(table -> table.getRows().stream().flatMap(row -> row.getCells().stream()).anyMatch(cell -> cell.getText().contains("EU"))));
        assertTrue(document.getBody().getTables().stream().anyMatch(table -> table.getRows().stream().flatMap(row -> row.getCells().stream()).anyMatch(cell -> cell.getText().contains("Key"))));
        assertTrue(document.getBody().getTables().stream().anyMatch(table -> table.getRows().stream().flatMap(row -> row.getCells().stream()).anyMatch(cell -> cell.getText().contains("Merged Cell"))));
        assertTrue(document.getBody().getTables().stream().anyMatch(table -> table.getRows().stream().flatMap(row -> row.getCells().stream()).anyMatch(cell -> cell.getText().contains("Finance"))));
        assertTrue(document.getBody().getReferences().stream().anyMatch(reference -> "NamedRange!A1:B2".equals(reference.getTarget())));
        assertTrue(document.getBody().getReferences().stream().anyMatch(reference -> "A4:B4".equals(reference.getTarget())));
        assertTrue(document.getBody().getDiagrams().isEmpty());
    }

    @Test
    void canonicalizationIsDeterministicForBenchmarkFixtures() throws Exception {
        assertDeterministic("v1-benchmark.docx");
        assertDeterministic("v1-benchmark.pptx");
        assertDeterministic("v1-benchmark.xlsx");
    }

    @Test
    void serializedBenchmarkOutputContainsCoreStructures() throws Exception {
        assertSerializedContains("v1-benchmark.docx", List.of("Benchmark Document", "label=\"h1\"", "label=\"h2\"", "Paragraph with bold", "Visit https://example.com", "Final paragraph", "<Table>"));
        assertSerializedContains("v1-benchmark.pptx", List.of("Overview", "CRM", "SAP", "<Table source-path=\"/ppt/slides/slide", "<Diagram source-path=\"/ppt/slides/slide", "<Diagram"));
        assertSerializedContains("v1-benchmark.xlsx", List.of("id=\"Applications\"", "id=\"Matrix\"", "id=\"NamedRange\"", "Application", "NamedRange!A1:B2", "A4:B4"));
    }

    @Test
    void docxSerializedElementsPreserveSourceOrder() throws Exception {
        Path file = copyFixture("v1-benchmark.docx");
        String xml = serialize(canonicalizer.canonicalize(file.toFile()));

        assertAppearsBefore(xml, "Section A", "First item");
        assertAppearsBefore(xml, "First item", "Second item");
        assertAppearsBefore(xml, "Second item", "Alpha");
        assertAppearsBefore(xml, "Alpha", "Beta");
        assertAppearsBefore(xml, "Visit https://example.com", "<Table");
        assertAppearsBefore(xml, "Beta", "App");
        assertAppearsBefore(xml, "App", "[A] -&gt; [B]");
        assertAppearsBefore(xml, "[A] -&gt; [B]", "Section B");
        assertAppearsBefore(xml, "Section B", "Final paragraph");
    }

    @Test
    void canonicalizesDocxFormulasAsMathMlAndPreservesOrder() throws Exception {
        Path file = copyFixture("v2-formulas.docx");

        CanonicalDocument document = canonicalizer.canonicalize(file.toFile());
        assertEquals("DOCX", document.getMetadata().getDocumentType());

        String xml = serialize(document);
        assertTrue(xml.contains("<math xmlns=\"http://www.w3.org/1998/Math/MathML\""));
        assertTrue(xml.contains(OmmlMathTransformer.MATHML_NS));
        assertFalse(xml.contains("<mrow/>"), "MathML should not contain empty mrow noise");

        org.w3c.dom.Document parsed = parseXml(xml);
        Element body = (Element) parsed.getElementsByTagNameNS(CanonicalNamespace.URI, "Body").item(0);
        assertFalse(hasDirectMathChild(body), "MathML must not be a direct Body child");
        assertTrue(hasParagraphWithMath(parsed), "At least one Paragraph should contain nested MathML");
        assertTrue(hasCellWithMath(parsed), "At least one Cell should contain nested MathML");
        assertFalse(xml.contains("<Text>CoverAmt Cov Perc"), "Flattened formula text should be removed from Paragraph payload");

        int bodyIndex = xml.indexOf("<Body>");
        int mathIndex = firstMathTagIndex(xml);
        int proseIndex = xml.indexOf("The cover priority for country risk");
        assertTrue(bodyIndex >= 0, "Missing token: <Body>");
        assertTrue(mathIndex >= 0, "Missing token: :math");
        assertTrue(proseIndex >= 0, "Missing token: The cover priority for country risk");
        assertTrue(bodyIndex < mathIndex && mathIndex < proseIndex,
                "Expected first MathML fragment to appear before narrative prose");
        assertAppearsBefore(xml, "Where:", "<Table");
    }

    @Test
    void canonicalizesDocxDiagramFixtureToSemanticDiagramNodes() throws Exception {
        Path file = copyFixture("v2-diagrams.docx");

        CanonicalDocument document = canonicalizer.canonicalize(file.toFile());
        assertEquals("DOCX", document.getMetadata().getDocumentType());
        assertFalse(document.getBody().getDiagrams().isEmpty());
        assertTrue(document.getBody().getDiagrams().stream().anyMatch(diagram -> !diagram.getNodes().isEmpty()));
        assertTrue(document.getBody().getDiagrams().stream().anyMatch(diagram ->
                diagram.getNodes().stream().anyMatch(node -> "image".equals(node.getGeometry()))));
        assertTrue(document.getBody().getDiagrams().stream().anyMatch(diagram ->
                diagram.getAnnotations().stream().anyMatch(annotation -> "asset".equals(annotation.getKind()))));

        String xml = serialize(document);
        assertAppearsBefore(xml, "Title 1", "<Diagram source-path=\"/word/document/p[2]/drawing[1]\"");
        assertAppearsBefore(xml, "<Diagram source-path=\"/word/document/p[2]/drawing[1]\"", "Title 2");
        assertAppearsBefore(xml, "Title 2", "<Diagram source-path=\"/word/document/p[3]/drawing[1]\"");
        assertTrue(xml.contains("kind=\"asset-text\""), "Expected extracted diagram text annotation");
        assertTrue(xml.contains("Start") || xml.contains("Calculate"),
                "Expected recovered EMF text content in canonical diagram annotations");
    }

    private void assertDeterministic(String fixtureName) throws Exception {
        Path file = copyFixture(fixtureName);
        String first = serialize(canonicalizer.canonicalize(file.toFile()));
        String second = serialize(canonicalizer.canonicalize(file.toFile()));
        assertEquals(first, second);
    }

    private void assertSerializedContains(String fixtureName, java.util.List<String> tokens) throws Exception {
        Path file = copyFixture(fixtureName);
        String actual = serialize(canonicalizer.canonicalize(file.toFile()));
        for (String token : tokens) {
            assertTrue(actual.contains(token), "Serialized XML for " + fixtureName + " should include: " + token);
        }
    }

    private String serialize(CanonicalDocument document) throws Exception {
        Path output = Files.createTempFile("ooxml-benchmark-canonical-", ".xml");
        serializer.write(document, output);
        return Files.readString(output).replace("\r\n", "\n");
    }

    private void assertAppearsBefore(String text, String left, String right) {
        int leftIndex = text.indexOf(left);
        int rightIndex = text.indexOf(right);
        assertTrue(leftIndex >= 0, "Missing token: " + left);
        assertTrue(rightIndex >= 0, "Missing token: " + right);
        assertTrue(leftIndex < rightIndex, "Expected order: " + left + " before " + right);
    }

    private int firstMathTagIndex(String xml) {
        int defaultNamespaceMath = xml.indexOf("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"");
        if (defaultNamespaceMath >= 0) {
            return defaultNamespaceMath;
        }
        int explicitPrefix = xml.indexOf("<m:math");
        if (explicitPrefix >= 0) {
            return explicitPrefix;
        }
        return xml.indexOf(":math");
    }

    private org.w3c.dom.Document parseXml(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        return factory.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(xml.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    }

    private boolean hasDirectMathChild(Element parent) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node instanceof Element element
                    && OmmlMathTransformer.MATHML_NS.equals(element.getNamespaceURI())
                    && "math".equals(element.getLocalName())) {
                return true;
            }
        }
        return false;
    }

    private boolean hasParagraphWithMath(org.w3c.dom.Document document) {
        NodeList paragraphs = document.getElementsByTagNameNS(CanonicalNamespace.URI, "Paragraph");
        for (int i = 0; i < paragraphs.getLength(); i++) {
            if (hasDirectMathChild((Element) paragraphs.item(i))) {
                return true;
            }
        }
        return false;
    }

    private boolean hasCellWithMath(org.w3c.dom.Document document) {
        NodeList cells = document.getElementsByTagNameNS(CanonicalNamespace.URI, "Cell");
        for (int i = 0; i < cells.getLength(); i++) {
            if (hasDirectMathChild((Element) cells.item(i))) {
                return true;
            }
        }
        return false;
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
