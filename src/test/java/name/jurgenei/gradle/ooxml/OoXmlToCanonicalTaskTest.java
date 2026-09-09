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

        OoXmlToCanonicalTask task = project.getTasks().register("ooxmlToCanonical", OoXmlToCanonicalTask.class).get();
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

        String docxXml = readCanonicalPayload(benchmarkZip, ".xml1");
        assertTrue(docxXml.contains("Benchmark Document"));
        assertTrue(docxXml.contains("<documentType>DOCX</documentType>"));
        assertTrue(docxXml.contains("label=\"h1\""));
        assertTrue(docxXml.contains("label=\"h2\""));
        assertTrue(docxXml.contains("source-path=\"/word/document/"));
        assertTrue(docxXml.contains("Paragraph with bold"));
        assertTrue(docxXml.contains("Visit https://example.com"));
        assertTrue(docxXml.contains("<list"));
        assertTrue(docxXml.contains("<table"));
        assertTrue(docxXml.contains("First item"));
        assertTrue(docxXml.contains("Alpha"));

        String formulaXml = readCanonicalPayload(formulasZip, ".xml1");
        assertTrue(formulaXml.contains("http://www.w3.org/1998/Math/MathML"));
        assertTrue(formulaXml.contains("<math xmlns=\"http://www.w3.org/1998/Math/MathML\""));
        assertTrue(!formulaXml.contains("<mrow/>"));
        assertTrue(formulaXml.contains("</para>\n        <para") || formulaXml.contains("</para>\r\n        <para"));
        assertTrue(!formulaXml.contains("</para>\n        <math xmlns=\"http://www.w3.org/1998/Math/MathML\""));
        assertTrue(!formulaXml.contains("</table>\n        <math xmlns=\"http://www.w3.org/1998/Math/MathML\""));
        assertTrue(!formulaXml.contains("<text>CoverAmt Cov Perc"));

        String diagramsXml = readCanonicalPayload(diagramsZip, ".xml1");
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
        assertTrue(diagramsXml.contains("<version>v2</version>"));
        assertTrue(diagramsXml.contains("see section a") || diagramsXml.contains("see section b") || diagramsXml.contains("see section c"));
        assertTrue(hasMediaEntries(diagramsZip));
        assertTrue(countMediaEntries(diagramsZip) >= 1);

        String xlsxXml = readCanonicalPayload(registerZip, ".xml1");
        assertTrue(countOccurrences(xlsxXml, "<table id=") >= 3);
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

        OoXmlToCanonicalTask task = project.getTasks().register("ooxmlToCanonicalLegacy", OoXmlToCanonicalTask.class).get();
        task.source(project.fileTree(docs.toFile(), spec -> spec.include("**/*.docx")));
        task.getOutputDirectory().set(project.getLayout().getBuildDirectory().dir("ooxml/canonical"));
        task.getLegacyXmlOutput().set(true);

        task.convert();

        Path canonicalRoot = projectDir.toPath().resolve("build/ooxml/canonical");
        Path legacyXml = canonicalRoot.resolve("v2-diagrams.xml1");
        assertTrue(Files.exists(legacyXml));
        assertTrue(!Files.exists(canonicalRoot.resolve("v2-diagrams_docx.zip")));

        String xml = Files.readString(legacyXml);
        assertTrue(xml.contains("<version>v2</version>"));
        assertTrue(xml.contains("href=\"media/image1.emf\"") || xml.contains("href=\"media/image2.emf\""));
    }

    @Test
    void serializesZipPayloadAsSexprWhenTargetExtensionIsSexpr() throws Exception {
        File projectDir = Files.createTempDirectory("ooxml-task-sexpr-package").toFile();
        Project project = ProjectBuilder.builder().withProjectDir(projectDir).build();

        Path docs = projectDir.toPath().resolve("docs");
        Files.createDirectories(docs);
        copyFixture(docs, "v1-benchmark.docx", "benchmark.docx");

        OoXmlToCanonicalTask task = project.getTasks().register("ooxmlToCanonicalSexpr", OoXmlToCanonicalTask.class).get();
        task.source(project.fileTree(docs.toFile(), spec -> spec.include("**/*.docx")));
        task.getOutputDirectory().set(project.getLayout().getBuildDirectory().dir("ooxml/canonical"));
        task.getTargetExtension().set(".sexpr");

        task.convert();

        Path zip = projectDir.toPath().resolve("build/ooxml/canonical/benchmark_docx.zip");
        assertTrue(Files.exists(zip));
        String sexpr = readCanonicalPayload(zip, ".sexpr");
        assertTrue(sexpr.startsWith("(."));
        assertTrue(sexpr.contains("(document"));
        assertTrue(sexpr.contains("Benchmark Document"));
    }

    @Test
    void serializesLegacyFlatOutputAsSexprWhenTargetExtensionIsSexpr() throws Exception {
        File projectDir = Files.createTempDirectory("ooxml-task-sexpr-flat").toFile();
        Project project = ProjectBuilder.builder().withProjectDir(projectDir).build();

        Path docs = projectDir.toPath().resolve("docs");
        Files.createDirectories(docs);
        copyFixture(docs, "v2-diagrams.docx", "v2-diagrams.docx");

        OoXmlToCanonicalTask task = project.getTasks().register("ooxmlToCanonicalLegacySexpr", OoXmlToCanonicalTask.class).get();
        task.source(project.fileTree(docs.toFile(), spec -> spec.include("**/*.docx")));
        task.getOutputDirectory().set(project.getLayout().getBuildDirectory().dir("ooxml/canonical"));
        task.getLegacyXmlOutput().set(true);
        task.getTargetExtension().set(".sexpr");

        task.convert();

        Path canonicalRoot = projectDir.toPath().resolve("build/ooxml/canonical");
        Path sexprFile = canonicalRoot.resolve("v2-diagrams.sexpr");
        assertTrue(Files.exists(sexprFile));
        assertTrue(!Files.exists(canonicalRoot.resolve("v2-diagrams_docx.zip")));

        String sexpr = Files.readString(sexprFile);
        assertTrue(sexpr.startsWith("(."));
        assertTrue(sexpr.contains("(version \"v2\")"));
    }

    @Test
    void supportsConfiguredRecognizerClassNames() throws Exception {
        File projectDir = Files.createTempDirectory("ooxml-task-recognizer-project").toFile();
        Project project = ProjectBuilder.builder().withProjectDir(projectDir).build();

        Path docs = projectDir.toPath().resolve("docs");
        Files.createDirectories(docs);
        copyFixture(docs, "v2-diagrams.docx", "v2-diagrams.docx");

        OoXmlToCanonicalTask task = project.getTasks().register("ooxmlToCanonicalRecognizers", OoXmlToCanonicalTask.class).get();
        task.source(project.fileTree(docs.toFile(), spec -> spec.include("**/*.docx")));
        task.getOutputDirectory().set(project.getLayout().getBuildDirectory().dir("ooxml/canonical"));
        task.getRecognizerClassNames().set(java.util.List.of("name.jurgenei.gradle.ooxml.recognizer.EmfAssetRecognizer"));

        task.convert();

        Path canonicalRoot = projectDir.toPath().resolve("build/ooxml/canonical");
        Path diagramsZip = canonicalRoot.resolve("v2-diagrams_docx.zip");
        assertTrue(Files.exists(diagramsZip));

        String xml = readCanonicalPayload(diagramsZip, ".xml1");
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

    private String readCanonicalPayload(Path zipPath, String extension) throws Exception {
        try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
            ZipEntry canonical = zipFile.getEntry("canonical" + extension);
            if (canonical == null) {
                throw new IllegalStateException("Missing canonical" + extension + " in package: " + zipPath);
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

