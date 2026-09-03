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
                <document xmlns=\"http://jurgenei.name/canonical\">
                  <metadata>
                    <documentId>sample</documentId>
                    <version>1</version>
                    <sourceFile>sample.docx</sourceFile>
                    <documentType>DOCX</documentType>
                  </metadata>
                  <body>
                    <para>
                      <text>Hello</text>
                    </para>
                  </body>
                </document>
                """, StandardCharsets.UTF_8);

        ValidateCanonicalTask task = project.getTasks().register("validateCanonical", ValidateCanonicalTask.class).get();
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
                    <document xmlns=\"http://jurgenei.name/canonical\" xmlns:g=\"http://graphml.graphdrawing.org/xmlns\">
                      <metadata>
                        <documentId>sample</documentId>
                        <version>v2</version>
                        <sourceFile>sample.docx</sourceFile>
                        <documentType>DOCX</documentType>
                      </metadata>
                      <body>
                        <g:graph href=\"media/image1.png\" source-path=\"/word/document/p[1]/drawing[1]\"/>
                      </body>
                    </document>
                    """.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }

        ValidateCanonicalTask task = project.getTasks().register("validateCanonicalZip", ValidateCanonicalTask.class).get();
        task.getInputDirectory().set(project.getLayout().getProjectDirectory().dir("build/ooxml/canonical"));

        assertDoesNotThrow(task::validate);
    }

    @Test
    void validatesCanonicalXmlWithChartEvidence() throws Exception {
        File projectDir = Files.createTempDirectory("ooxml-validate-chart-project").toFile();
        Project project = ProjectBuilder.builder().withProjectDir(projectDir).build();

        Path canonical = projectDir.toPath().resolve("build/ooxml/canonical");
        Files.createDirectories(canonical);
        Files.writeString(canonical.resolve("chart.xml"), """
                <document xmlns="http://jurgenei.name/canonical">
                  <metadata>
                    <documentId>chart</documentId>
                    <version>v1</version>
                    <sourceFile>chart.xlsx</sourceFile>
                    <documentType>XLSX</documentType>
                  </metadata>
                  <body>
                    <chart source-path="/xl/charts/chart1.xml" href="media/chart1.xml">
                      <title>Revenue trend</title>
                      <legend>Region</legend>
                      <axis role="x">
                        <label>Quarter</label>
                      </axis>
                      <axis role="y">
                        <label>Revenue</label>
                        <unit>EUR</unit>
                      </axis>
                      <series>
                        <name>NL</name>
                        <value>10</value>
                        <value>12</value>
                      </series>
                    </chart>
                  </body>
                </document>
                """, StandardCharsets.UTF_8);

        ValidateCanonicalTask task = project.getTasks().register("validateCanonicalChart", ValidateCanonicalTask.class).get();
        task.getInputDirectory().set(project.getLayout().getProjectDirectory().dir("build/ooxml/canonical"));

        assertDoesNotThrow(task::validate);
    }
}

