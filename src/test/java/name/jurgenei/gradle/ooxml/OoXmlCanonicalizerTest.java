package name.jurgenei.gradle.ooxml;

import name.jurgenei.gradle.ooxml.canonical.CanonicalDocument;
import org.junit.jupiter.api.Test;

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
        assertTrue(document.getBody().getParagraphs().stream().anyMatch(p -> p.getSourcePath() != null && p.getSourcePath().startsWith("/word/document/")));
        assertEquals(2, document.getBody().getLists().size());
        assertTrue(document.getBody().getLists().stream().anyMatch(list -> list.isOrdered() && list.getItems().stream().anyMatch(item -> item.getText().contains("First item"))));
        assertTrue(document.getBody().getLists().stream().anyMatch(list -> !list.isOrdered() && list.getItems().stream().anyMatch(item -> item.getText().contains("Alpha"))));
        assertFalse(document.getBody().getTables().isEmpty());
        assertTrue(document.getBody().getTables().stream().anyMatch(table -> table.getRows().stream().flatMap(row -> row.getCells().stream()).anyMatch(cell -> cell.getText().contains("App"))));
        assertTrue(document.getBody().getTables().stream().anyMatch(table -> table.getRows().stream().flatMap(row -> row.getCells().stream()).anyMatch(cell -> cell.getText().contains("Sales"))));
        assertEquals(0, document.getBody().getLinks().size());
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
        assertFalse(document.getBody().getDiagrams().isEmpty());
        assertTrue(document.getBody().getDiagrams().stream().anyMatch(diagram -> diagram.getShapes().stream().anyMatch(shape -> "Rounded Rectangle 2".equals(shape.getLabel()))));
        assertTrue(document.getBody().getDiagrams().stream().anyMatch(diagram -> diagram.getConnectors().stream().anyMatch(connector -> "3".equals(connector.getSource()) && "4".equals(connector.getTarget()))));
    }

    @Test
    void canonicalizesXlsxBenchmark() throws Exception {
        Path file = copyFixture("v1-benchmark.xlsx");

        CanonicalDocument document = canonicalizer.canonicalize(file.toFile());

        assertEquals("XLSX", document.getMetadata().getDocumentType());
        assertTrue(document.getBody().getParagraphs().stream().anyMatch(p -> p.getText().contains("A1=Application")));
        assertTrue(document.getBody().getParagraphs().stream().anyMatch(p -> p.getText().contains("B1=EU")));
        assertTrue(document.getBody().getParagraphs().stream().anyMatch(p -> p.getText().contains("A4=Merged Cell")));
        assertTrue(document.getBody().getParagraphs().stream().anyMatch(p -> p.getSourcePath() != null && p.getSourcePath().contains("/xl/worksheets/sheet1.xml/A1")));
        assertFalse(document.getBody().getTables().isEmpty());
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
        assertSerializedContains("v1-benchmark.docx", List.of("Benchmark Document", "label=\"h1\"", "label=\"h2\"", "Paragraph with bold", "Visit https://example.com", "<Table>"));
        assertSerializedContains("v1-benchmark.pptx", List.of("Overview", "Rounded Rectangle 2", "<Diagram>"));
        assertSerializedContains("v1-benchmark.xlsx", List.of("A1=Application", "NamedRange!A1:B2", "A4:B4"));
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
