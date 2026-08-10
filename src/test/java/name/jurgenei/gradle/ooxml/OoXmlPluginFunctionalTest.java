package name.jurgenei.gradle.ooxml;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OoXmlPluginFunctionalTest {
    @TempDir
    Path tempDir;

    @Test
    void runsTaskInConsumerBuild() throws Exception {
        Path projectDir = tempDir.resolve("consumer");
        Files.createDirectories(projectDir.resolve("docs"));
        copyFixture(projectDir.resolve("docs"), "v1-benchmark.docx", "v1-benchmark.docx");

        Files.writeString(projectDir.resolve("settings.gradle"), "rootProject.name = 'ooxml-functional-test'\n", StandardCharsets.UTF_8);
        Files.writeString(projectDir.resolve("build.gradle"), """
                plugins {
                    id 'name.jurgenei.gradle.ooxml'
                }

                tasks.named('ooxmlToCanonical', name.jurgenei.gradle.ooxml.OoXmlToCanonicalTask) {
                    source(fileTree(layout.projectDirectory.dir('docs')) {
                        include '**/*.docx'
                    })
                }
                """, StandardCharsets.UTF_8);

        BuildResult result = GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withArguments("ooxmlToCanonical")
                .withPluginClasspath()
                .build();

        assertTrue(result.getOutput().contains("BUILD SUCCESSFUL"));
        assertTrue(Files.exists(projectDir.resolve("build/ooxml/canonical/v1-benchmark.xml")));
    }

    @Test
    void convertsAndValidatesAllFormatsEndToEnd() throws Exception {
        Path projectDir = tempDir.resolve("consumer-e2e");
        Files.createDirectories(projectDir.resolve("docs"));
        copyFixture(projectDir.resolve("docs"), "v1-benchmark.docx", "v1-benchmark.docx");
        copyFixture(projectDir.resolve("docs"), "v1-benchmark.pptx", "slides.pptx");
        copyFixture(projectDir.resolve("docs"), "v1-benchmark.xlsx", "register.xlsx");

        Files.writeString(projectDir.resolve("settings.gradle"), "rootProject.name = 'ooxml-functional-e2e'\n", StandardCharsets.UTF_8);
        Files.writeString(projectDir.resolve("build.gradle"), """
                plugins {
                    id 'name.jurgenei.gradle.ooxml'
                }

                tasks.named('ooxmlToCanonical', name.jurgenei.gradle.ooxml.OoXmlToCanonicalTask) {
                    source(fileTree(layout.projectDirectory.dir('docs')) {
                        include '**/*.docx', '**/*.pptx', '**/*.xlsx'
                    })
                }

                tasks.named('validateCanonical', name.jurgenei.gradle.ooxml.ValidateCanonicalTask) {
                    inputDirectory.set(layout.buildDirectory.dir('ooxml/canonical'))
                    dependsOn(tasks.named('ooxmlToCanonical'))
                }
                """, StandardCharsets.UTF_8);

        BuildResult result = GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withArguments("ooxmlToCanonical", "validateCanonical")
                .withPluginClasspath()
                .build();

        assertTrue(result.getOutput().contains("BUILD SUCCESSFUL"));
        assertTrue(Files.exists(projectDir.resolve("build/ooxml/canonical/v1-benchmark.xml")));
        assertTrue(Files.exists(projectDir.resolve("build/ooxml/canonical/slides.xml")));
        assertTrue(Files.exists(projectDir.resolve("build/ooxml/canonical/register.xml")));
    }

    @Test
    void bootstrapsSchematronWithXmlPluginFromOoXmlExtension() throws Exception {
        Path projectDir = tempDir.resolve("consumer-schematron");
        Files.createDirectories(projectDir.resolve("src/main/xml"));

        File ooxmlPluginDir = new File(System.getProperty("user.dir"));
        File xmlPluginDir = new File(ooxmlPluginDir.getParentFile(), "gradle-xml-plugin");

        Files.writeString(projectDir.resolve("settings.gradle"), """
                pluginManagement {
                    includeBuild('%s')
                }
                rootProject.name = 'ooxml-schematron-e2e'
                """.formatted(xmlPluginDir.getAbsolutePath().replace('\\', '/')), StandardCharsets.UTF_8);

        Files.writeString(projectDir.resolve("build.gradle"), """
                plugins {
                    id 'name.jurgenei.gradle.ooxml'
                    id 'name.jurgenei.gradle.xml'
                }

                tasks.register('bootstrapCanonicalSchematron', name.jurgenei.gradle.xml.SchematronBootstrapTask) {
                    def ooxmlExt = project.extensions.getByType(name.jurgenei.gradle.ooxml.OoXmlExtension)
                    schemaUrl(ooxmlExt.canonicalSchemaUrl.get())
                    output 'src/main/schematron/canonical-observation.sch'
                }

                tasks.register('validateCanonicalSchematron', name.jurgenei.gradle.xml.SchematronTask) {
                    dependsOn tasks.named('bootstrapCanonicalSchematron')
                    schema.set(layout.projectDirectory.file('src/main/schematron/canonical-observation.sch'))
                    source 'src/main/xml/canonical.xml'
                    outputDir.set(layout.buildDirectory.dir('reports/schematron'))
                    reportFormat.set(name.jurgenei.gradle.xml.validation.ReportFormat.SVRL_AND_JUNIT)
                    failOnError.set(true)
                }
                """, StandardCharsets.UTF_8);

        Files.writeString(projectDir.resolve("src/main/xml/canonical.xml"), """
                <c:Document xmlns:c='http://jurgenei.name/canonical'>
                  <c:Metadata>
                    <c:DocumentId>sample</c:DocumentId>
                    <c:Version>1</c:Version>
                    <c:SourceFile>sample.docx</c:SourceFile>
                    <c:DocumentType>DOCX</c:DocumentType>
                  </c:Metadata>
                  <c:Body>
                    <c:Paragraph source-path='/word/document/p[1]'>
                      <c:Text>Hello</c:Text>
                    </c:Paragraph>
                  </c:Body>
                </c:Document>
                """, StandardCharsets.UTF_8);

        BuildResult result = GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withArguments("bootstrapCanonicalSchematron", "validateCanonicalSchematron")
                .withPluginClasspath()
                .build();

        assertTrue(result.getOutput().contains("BUILD SUCCESSFUL"));
        assertTrue(Files.exists(projectDir.resolve("src/main/schematron/canonical-observation.sch")));
        assertTrue(Files.exists(projectDir.resolve("build/reports/schematron/canonical.svrl.xml")));
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

