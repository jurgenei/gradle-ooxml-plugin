package name.jurgenei.gradle.ooxml;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ValidateCanonicalTaskTest {
    @Test
    void validatesGeneratedCanonicalXml() throws Exception {
        File projectDir = Files.createTempDirectory("ooxml-validate-project").toFile();
        Project project = ProjectBuilder.builder().withProjectDir(projectDir).build();

        Path canonical = projectDir.toPath().resolve("build/ooxml/canonical");
        Files.createDirectories(canonical);
        Files.writeString(canonical.resolve("sample.xml"), """
                <Document xmlns=\"http://jurgenei.name/canonical\">
                  <Metadata>
                    <DocumentId>sample</DocumentId>
                    <Version>1</Version>
                    <SourceFile>sample.docx</SourceFile>
                    <DocumentType>DOCX</DocumentType>
                  </Metadata>
                  <Body>
                    <Paragraph>
                      <Text>Hello</Text>
                    </Paragraph>
                  </Body>
                </Document>
                """, StandardCharsets.UTF_8);

        ValidateCanonicalTask task = project.getTasks().create("validateCanonical", ValidateCanonicalTask.class);
        task.getInputDirectory().set(project.getLayout().getProjectDirectory().dir("build/ooxml/canonical"));

        assertDoesNotThrow(task::validate);
    }

    @Test
    void validatesCanonicalXmlInsideZipPackage() throws Exception {
        File projectDir = Files.createTempDirectory("ooxml-validate-zip-project").toFile();
        Project project = ProjectBuilder.builder().withProjectDir(projectDir).build();

        Path canonical = projectDir.toPath().resolve("build/ooxml/canonical");
        Files.createDirectories(canonical);
        Path zipPath = canonical.resolve("sample_docx.zip");
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            zip.putNextEntry(new ZipEntry("canonical.xml"));
            zip.write("""
                    <Document xmlns=\"http://jurgenei.name/canonical\" xmlns:g=\"http://graphml.graphdrawing.org/xmlns\">
                      <Metadata>
                        <DocumentId>sample</DocumentId>
                        <Version>v2</Version>
                        <SourceFile>sample.docx</SourceFile>
                        <DocumentType>DOCX</DocumentType>
                      </Metadata>
                      <Body>
                        <g:graph href=\"media/image1.png\" source-path=\"/word/document/p[1]/drawing[1]\"/>
                      </Body>
                    </Document>
                    """.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }

        ValidateCanonicalTask task = project.getTasks().create("validateCanonicalZip", ValidateCanonicalTask.class);
        task.getInputDirectory().set(project.getLayout().getProjectDirectory().dir("build/ooxml/canonical"));

        assertDoesNotThrow(task::validate);
    }
}

