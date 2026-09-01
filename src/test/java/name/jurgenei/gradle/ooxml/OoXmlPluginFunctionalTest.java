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
        assertTrue(Files.exists(projectDir.resolve("build/ooxml/canonical/v1-benchmark_docx.zip")));
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
        assertTrue(Files.exists(projectDir.resolve("build/ooxml/canonical/v1-benchmark_docx.zip")));
        assertTrue(Files.exists(projectDir.resolve("build/ooxml/canonical/slides_pptx.zip")));
        assertTrue(Files.exists(projectDir.resolve("build/ooxml/canonical/register_xlsx.zip")));
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
                <document xmlns='http://jurgenei.name/canonical'>
                  <metadata>
                    <documentId>sample</documentId>
                    <version>1</version>
                    <sourceFile>sample.docx</sourceFile>
                    <documentType>DOCX</documentType>
                  </metadata>
                  <body>
                    <para source-path='/word/document/p[1]'>
                      <text>Hello</text>
                    </para>
                  </body>
                </document>
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

    @Test
    void supportsLegacyXmlOutputFlagInConsumerBuild() throws Exception {
        Path projectDir = tempDir.resolve("consumer-legacy");
        Files.createDirectories(projectDir.resolve("docs"));
        copyFixture(projectDir.resolve("docs"), "v2-diagrams.docx", "v2-diagrams.docx");

        Files.writeString(projectDir.resolve("settings.gradle"), "rootProject.name = 'ooxml-functional-legacy'\n", StandardCharsets.UTF_8);
        Files.writeString(projectDir.resolve("build.gradle"), """
                plugins {
                    id 'name.jurgenei.gradle.ooxml'
                }

                tasks.named('ooxmlToCanonical', name.jurgenei.gradle.ooxml.OoXmlToCanonicalTask) {
                    source(fileTree(layout.projectDirectory.dir('docs')) {
                        include '**/*.docx'
                    })
                    legacyXmlOutput.set(true)
                }
                """, StandardCharsets.UTF_8);

        BuildResult result = GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withArguments("ooxmlToCanonical")
                .withPluginClasspath()
                .build();

        assertTrue(result.getOutput().contains("BUILD SUCCESSFUL"));
        assertTrue(Files.exists(projectDir.resolve("build/ooxml/canonical/v2-diagrams.xml")));
        assertTrue(!Files.exists(projectDir.resolve("build/ooxml/canonical/v2-diagrams_docx.zip")));
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

