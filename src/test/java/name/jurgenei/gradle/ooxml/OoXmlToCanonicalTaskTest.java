package name.jurgenei.gradle.ooxml;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

class OoXmlToCanonicalTaskTest {
    @Test
    void convertsMixedSourceTreeToCanonicalXml() throws Exception {
        File projectDir = Files.createTempDirectory("ooxml-task-project").toFile();
        Project project = ProjectBuilder.builder().withProjectDir(projectDir).build();

        Path docs = projectDir.toPath().resolve("docs");
        Files.createDirectories(docs);
        copyFixture(docs, "v1-benchmark.docx", "benchmark.docx");
        copyFixture(docs, "v2-formulas.docx", "formulas.docx");
        copyFixture(docs, "v2-diagrams.docx", "v2-diagrams.docx");
        copyFixture(docs, "v1-benchmark.pptx", "slides.pptx");
        copyFixture(docs, "v1-benchmark.xlsx", "register.xlsx");

        OoXmlToCanonicalTask task = project.getTasks().create("ooxmlToCanonical", OoXmlToCanonicalTask.class);
        task.source(project.fileTree(docs.toFile(), spec -> spec.include("**/*.docx", "**/*.pptx", "**/*.xlsx")));
        task.getOutputDirectory().set(project.getLayout().getBuildDirectory().dir("ooxml/canonical"));

        task.convert();

        Path canonicalRoot = projectDir.toPath().resolve("build/ooxml/canonical");
        Path benchmarkZip = canonicalRoot.resolve("benchmark_docx.zip");
        Path formulasZip = canonicalRoot.resolve("formulas_docx.zip");
        Path diagramsZip = canonicalRoot.resolve("v2-diagrams_docx.zip");
        Path slidesZip = canonicalRoot.resolve("slides_pptx.zip");
        Path registerZip = canonicalRoot.resolve("register_xlsx.zip");

        assertTrue(Files.exists(benchmarkZip));
        assertTrue(Files.exists(formulasZip));
        assertTrue(Files.exists(diagramsZip));
        assertTrue(Files.exists(slidesZip));
        assertTrue(Files.exists(registerZip));

        String docxXml = readCanonicalXml(benchmarkZip);
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

        String formulaXml = readCanonicalXml(formulasZip);
        assertTrue(formulaXml.contains("http://www.w3.org/1998/Math/MathML"));
        assertTrue(formulaXml.contains("<math xmlns=\"http://www.w3.org/1998/Math/MathML\""));
        assertTrue(!formulaXml.contains("<mrow/>"));
        assertTrue(formulaXml.contains("</Paragraph>\n        <Paragraph") || formulaXml.contains("</Paragraph>\r\n        <Paragraph"));
        assertTrue(!formulaXml.contains("</Paragraph>\n        <math xmlns=\"http://www.w3.org/1998/Math/MathML\""));
        assertTrue(!formulaXml.contains("</Table>\n        <math xmlns=\"http://www.w3.org/1998/Math/MathML\""));
        assertTrue(!formulaXml.contains("<Text>CoverAmt Cov Perc"));

        String diagramsXml = readCanonicalXml(diagramsZip);
        assertTrue(diagramsXml.contains("<graph xmlns=\"http://graphml.graphdrawing.org/xmlns\""));
        assertTrue(diagramsXml.contains("href=\"media/"));
        assertTrue(diagramsXml.contains("<node "));
        assertTrue(diagramsXml.contains("semantic=\"process\""));
        assertTrue(diagramsXml.contains("semantic=\"flow\""));
        assertTrue(diagramsXml.contains("<group id="));
        assertTrue(diagramsXml.contains("kind=\"asset\""));
        assertTrue(!diagramsXml.contains("<g:graph"));
        assertTrue(diagramsXml.contains("source-path=\"/word/document/p[2]/drawing[1]\""));
        assertTrue(diagramsXml.contains("source-path=\"/word/document/p[3]/drawing[1]\""));
        assertTrue(diagramsXml.contains("kind=\"asset-text\""));
        assertTrue(diagramsXml.contains("kind=\"inferred-flow\""));
        assertTrue(diagramsXml.contains("<Version>v2</Version>"));
        assertTrue(diagramsXml.contains("see section a") || diagramsXml.contains("see section b") || diagramsXml.contains("see section c"));
        assertTrue(hasMediaEntries(diagramsZip));
        assertEquals(2, countMediaEntries(diagramsZip));

        String xlsxXml = readCanonicalXml(registerZip);
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

    @Test
    void supportsLegacyFlatXmlOutputWhenEnabled() throws Exception {
        File projectDir = Files.createTempDirectory("ooxml-task-legacy-project").toFile();
        Project project = ProjectBuilder.builder().withProjectDir(projectDir).build();

        Path docs = projectDir.toPath().resolve("docs");
        Files.createDirectories(docs);
        copyFixture(docs, "v2-diagrams.docx", "v2-diagrams.docx");

        OoXmlToCanonicalTask task = project.getTasks().create("ooxmlToCanonicalLegacy", OoXmlToCanonicalTask.class);
        task.source(project.fileTree(docs.toFile(), spec -> spec.include("**/*.docx")));
        task.getOutputDirectory().set(project.getLayout().getBuildDirectory().dir("ooxml/canonical"));
        task.getLegacyXmlOutput().set(true);

        task.convert();

        Path canonicalRoot = projectDir.toPath().resolve("build/ooxml/canonical");
        Path legacyXml = canonicalRoot.resolve("v2-diagrams.xml");
        assertTrue(Files.exists(legacyXml));
        assertTrue(!Files.exists(canonicalRoot.resolve("v2-diagrams_docx.zip")));

        String xml = Files.readString(legacyXml);
        assertTrue(xml.contains("<Version>v2</Version>"));
        assertTrue(xml.contains("href=\"media/image1.emf\"") || xml.contains("href=\"media/image2.emf\""));
    }

    @Test
    void supportsConfiguredRecognizerClassNames() throws Exception {
        File projectDir = Files.createTempDirectory("ooxml-task-recognizer-project").toFile();
        Project project = ProjectBuilder.builder().withProjectDir(projectDir).build();

        Path docs = projectDir.toPath().resolve("docs");
        Files.createDirectories(docs);
        copyFixture(docs, "v2-diagrams.docx", "v2-diagrams.docx");

        OoXmlToCanonicalTask task = project.getTasks().create("ooxmlToCanonicalRecognizers", OoXmlToCanonicalTask.class);
        task.source(project.fileTree(docs.toFile(), spec -> spec.include("**/*.docx")));
        task.getOutputDirectory().set(project.getLayout().getBuildDirectory().dir("ooxml/canonical"));
        task.getRecognizerClassNames().set(java.util.List.of("name.jurgenei.gradle.ooxml.recognizer.EmfAssetRecognizer"));

        task.convert();

        Path canonicalRoot = projectDir.toPath().resolve("build/ooxml/canonical");
        Path diagramsZip = canonicalRoot.resolve("v2-diagrams_docx.zip");
        assertTrue(Files.exists(diagramsZip));

        String xml = readCanonicalXml(diagramsZip);
        assertTrue(xml.contains("kind=\"emf-stats\""));
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

    private String readCanonicalXml(Path zipPath) throws Exception {
        try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
            ZipEntry canonical = zipFile.getEntry("canonical.xml");
            if (canonical == null) {
                throw new IllegalStateException("Missing canonical.xml in package: " + zipPath);
            }
            try (InputStream input = zipFile.getInputStream(canonical)) {
                return new String(input.readAllBytes(), StandardCharsets.UTF_8);
            }
        }
    }

    private boolean hasMediaEntries(Path zipPath) throws Exception {
        try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.isDirectory() && entry.getName().startsWith("media/")) {
                    return true;
                }
            }
        }
        return false;
    }

    private int countMediaEntries(Path zipPath) throws Exception {
        int count = 0;
        try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.isDirectory() && entry.getName().startsWith("media/")) {
                    count++;
                }
            }
        }
        return count;
    }
}

