package name.jurgenei.gradle.ooxml;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OoXmlToCanonicalTaskTest {
    @Test
    void convertsMixedSourceTreeToCanonicalXml() throws Exception {
        File projectDir = Files.createTempDirectory("ooxml-task-project").toFile();
        Project project = ProjectBuilder.builder().withProjectDir(projectDir).build();

        Path docs = projectDir.toPath().resolve("docs");
        Files.createDirectories(docs);
        copyFixture(docs, "v1-benchmark.docx", "benchmark.docx");
        copyFixture(docs, "v2-formulas.docx", "formulas.docx");
        copyFixture(docs, "v1-benchmark.pptx", "slides.pptx");
        copyFixture(docs, "v1-benchmark.xlsx", "register.xlsx");

        OoXmlToCanonicalTask task = project.getTasks().create("ooxmlToCanonical", OoXmlToCanonicalTask.class);
        task.source(project.fileTree(docs.toFile(), spec -> spec.include("**/*.docx", "**/*.pptx", "**/*.xlsx")));
        task.getOutputDirectory().set(project.getLayout().getBuildDirectory().dir("ooxml/canonical"));

        task.convert();

        Path canonicalRoot = projectDir.toPath().resolve("build/ooxml/canonical");
        assertTrue(Files.exists(canonicalRoot.resolve("benchmark.xml")));
        assertTrue(Files.exists(canonicalRoot.resolve("formulas.xml")));
        assertTrue(Files.exists(canonicalRoot.resolve("slides.xml")));
        assertTrue(Files.exists(canonicalRoot.resolve("register.xml")));

        String docxXml = Files.readString(canonicalRoot.resolve("benchmark.xml"));
        assertTrue(docxXml.contains("Benchmark Document"));
        assertTrue(docxXml.contains("<DocumentType>DOCX</DocumentType>"));
        assertTrue(docxXml.contains("label=\"h1\""));
        assertTrue(docxXml.contains("label=\"h2\""));
        assertTrue(docxXml.contains("source-path=\"/word/document/"));
        assertTrue(docxXml.contains("Paragraph with bold"));
        assertTrue(docxXml.contains("Visit https://example.com"));
        assertTrue(docxXml.contains("List"));
        assertTrue(docxXml.contains("Table"));
        assertTrue(docxXml.contains("First item"));
        assertTrue(docxXml.contains("Alpha"));

        String formulaXml = Files.readString(canonicalRoot.resolve("formulas.xml"));
        assertTrue(formulaXml.contains("http://www.w3.org/1998/Math/MathML"));
        assertTrue(formulaXml.contains("<math xmlns=\"http://www.w3.org/1998/Math/MathML\""));
        assertTrue(!formulaXml.contains("<mrow/>"));
        assertTrue(formulaXml.contains("</Paragraph>\n        <Paragraph") || formulaXml.contains("</Paragraph>\r\n        <Paragraph"));
        assertTrue(!formulaXml.contains("</Paragraph>\n        <math xmlns=\"http://www.w3.org/1998/Math/MathML\""));
        assertTrue(!formulaXml.contains("</Table>\n        <math xmlns=\"http://www.w3.org/1998/Math/MathML\""));
        assertTrue(!formulaXml.contains("<Text>CoverAmt Cov Perc"));

        String xlsxXml = Files.readString(canonicalRoot.resolve("register.xml"));
        assertTrue(countOccurrences(xlsxXml, "<Table id=") >= 3);
        assertTrue(xlsxXml.contains("id=\"Applications\""));
        assertTrue(xlsxXml.contains("id=\"Matrix\""));
        assertTrue(xlsxXml.contains("id=\"NamedRange\""));
        assertTrue(xlsxXml.contains("Application"));
        assertTrue(xlsxXml.contains("EU"));
        assertTrue(xlsxXml.contains("Key"));
        assertTrue(xlsxXml.contains("NamedRange!A1:B2"));
        assertTrue(xlsxXml.contains("A4:B4"));
    }

    private int countOccurrences(String text, String token) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(token, index)) >= 0) {
            count++;
            index += token.length();
        }
        return count;
    }

    private void copyFixture(Path targetDirectory, String fixtureName, String targetName) throws Exception {
        Files.createDirectories(targetDirectory);
        Path target = targetDirectory.resolve(targetName);
        try (InputStream input = getClass().getResourceAsStream("/ooxml/" + fixtureName)) {
            if (input == null) {
                throw new IllegalStateException("Missing fixture: " + fixtureName);
            }
            Files.copy(input, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }
}

