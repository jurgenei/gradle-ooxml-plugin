package name.jurgenei.gradle.ooxml;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
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
        copyFixture(projectDir.resolve("docs"), "sample_v3.docx", "sample_v3.docx");

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
        assertTrue(Files.exists(projectDir.resolve("build/ooxml/canonical/sample_v3.xml")));
    }

    @Test
    void convertsAndValidatesAllFormatsEndToEnd() throws Exception {
        Path projectDir = tempDir.resolve("consumer-e2e");
        Files.createDirectories(projectDir.resolve("docs"));
        copyFixture(projectDir.resolve("docs"), "sample_v3.docx", "sample_v3.docx");
        copyFixture(projectDir.resolve("docs"), "sample.pptx", "slides.pptx");
        copyFixture(projectDir.resolve("docs"), "sample.xlsx", "register.xlsx");

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
        assertTrue(Files.exists(projectDir.resolve("build/ooxml/canonical/sample_v3.xml")));
        assertTrue(Files.exists(projectDir.resolve("build/ooxml/canonical/slides.xml")));
        assertTrue(Files.exists(projectDir.resolve("build/ooxml/canonical/register.xml")));
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

