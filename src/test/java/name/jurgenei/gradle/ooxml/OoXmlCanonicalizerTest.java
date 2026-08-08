package name.jurgenei.gradle.ooxml;

import name.jurgenei.gradle.ooxml.canonical.CanonicalDocument;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OoXmlCanonicalizerTest {
    private final OoXmlCanonicalizer canonicalizer = new OoXmlCanonicalizer();

    @Test
    void canonicalizesDocx() throws Exception {
        Path file = copyFixture("sample_v3.docx");

        CanonicalDocument document = canonicalizer.canonicalize(file.toFile());

        assertEquals("sample", document.getMetadata().getDocumentId());
        assertEquals("3", document.getMetadata().getVersion());
        assertEquals("DOCX", document.getMetadata().getDocumentType());
        assertFalse(document.getBody().getParagraphs().isEmpty());
        assertTrue(document.getBody().getParagraphs().stream().anyMatch(p -> p.getText().contains("Hello from DOCX")));
        assertTrue(document.getBody().getParagraphs().stream().anyMatch(p -> "sample_v3.docx".equals(p.getSourceDocument())));
        assertTrue(document.getBody().getParagraphs().stream().anyMatch(p -> p.getSourcePath() != null && p.getSourcePath().startsWith("/word/document/")));
        assertFalse(document.getBody().getLists().isEmpty());
        assertTrue(document.getBody().getLists().stream().anyMatch(list -> list.getItems().stream().anyMatch(item -> item.getText().contains("List item"))));
        assertFalse(document.getBody().getTables().isEmpty());
        assertTrue(document.getBody().getTables().stream().anyMatch(table -> table.getRows().stream().flatMap(row -> row.getCells().stream()).anyMatch(cell -> cell.getText().contains("R1C1"))));
        assertTrue(document.getBody().getLinks().stream().anyMatch(link -> "https://example.com/docx".equals(link.getTarget())));
        assertTrue(document.getBody().getReferences().stream().anyMatch(reference -> "bookmark-1".equals(reference.getTarget())));
        assertFalse(document.getBody().getDiagrams().isEmpty());
        assertTrue(document.getBody().getDiagrams().stream().anyMatch(diagram -> diagram.getShapes().stream().anyMatch(shape -> "Docx Shape A".equals(shape.getLabel()))));
    }

    @Test
    void canonicalizesPptx() throws Exception {
        Path file = copyFixture("sample.pptx");

        CanonicalDocument document = canonicalizer.canonicalize(file.toFile());

        assertEquals("PPTX", document.getMetadata().getDocumentType());
        assertTrue(document.getBody().getParagraphs().stream().anyMatch(p -> p.getText().contains("Slide 1 Title")));
        assertTrue(document.getBody().getParagraphs().stream().anyMatch(p -> p.getSourcePath() != null && p.getSourcePath().startsWith("/ppt/slides/")));
        assertFalse(document.getBody().getLists().isEmpty());
        assertTrue(document.getBody().getLists().stream().anyMatch(list -> list.getItems().stream().anyMatch(item -> item.getText().contains("Bullet"))));
        assertFalse(document.getBody().getTables().isEmpty());
        assertTrue(document.getBody().getTables().stream().anyMatch(table -> table.getRows().stream().flatMap(row -> row.getCells().stream()).anyMatch(cell -> cell.getText().contains("P11"))));
        assertTrue(document.getBody().getLinks().stream().anyMatch(link -> "https://example.com/pptx".equals(link.getTarget())));
        assertFalse(document.getBody().getDiagrams().isEmpty());
        assertTrue(document.getBody().getDiagrams().stream().anyMatch(diagram -> diagram.getShapes().stream().anyMatch(shape -> "Ppt Shape A".equals(shape.getLabel()))));
        assertTrue(document.getBody().getDiagrams().stream().anyMatch(diagram -> diagram.getConnectors().stream().anyMatch(connector -> "201".equals(connector.getSource()) && "202".equals(connector.getTarget()))));
    }

    @Test
    void canonicalizesXlsx() throws Exception {
        Path file = copyFixture("sample.xlsx");

        CanonicalDocument document = canonicalizer.canonicalize(file.toFile());

        assertEquals("XLSX", document.getMetadata().getDocumentType());
        assertTrue(document.getBody().getParagraphs().stream().anyMatch(p -> p.getText().contains("A1=Cell From XLSX")));
        assertTrue(document.getBody().getParagraphs().stream().anyMatch(p -> p.getSourcePath() != null && p.getSourcePath().contains("/A1")));
        assertFalse(document.getBody().getTables().isEmpty());
        assertTrue(document.getBody().getTables().stream().anyMatch(table -> table.getRows().stream().flatMap(row -> row.getCells().stream()).anyMatch(cell -> cell.getText().contains("Cell From XLSX"))));
        assertTrue(document.getBody().getLinks().stream().anyMatch(link -> "https://example.com/xlsx".equals(link.getTarget())));
        assertTrue(document.getBody().getReferences().stream().anyMatch(reference -> "Sheet1!A2".equals(reference.getTarget())));
        assertFalse(document.getBody().getDiagrams().isEmpty());
        assertTrue(document.getBody().getDiagrams().stream().anyMatch(diagram -> diagram.getShapes().stream().anyMatch(shape -> "Sheet Shape A".equals(shape.getLabel()))));
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
