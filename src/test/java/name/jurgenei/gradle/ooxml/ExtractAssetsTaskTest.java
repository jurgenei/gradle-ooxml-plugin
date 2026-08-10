package name.jurgenei.gradle.ooxml;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtractAssetsTaskTest {
    @Test
    void extractsMediaFromAllFormats() throws Exception {
        File projectDir = Files.createTempDirectory("ooxml-assets-project").toFile();
        Project project = ProjectBuilder.builder().withProjectDir(projectDir).build();

        Path docs = projectDir.toPath().resolve("docs");
        Files.createDirectories(docs);
        copyFixture(docs, "v1-benchmark.docx", "contract_v1.docx");
        copyFixture(docs, "v1-benchmark.pptx", "slides.pptx");
        copyFixture(docs, "v1-benchmark.xlsx", "register.xlsx");

        ExtractAssetsTask task = project.getTasks().create("extractAssets", ExtractAssetsTask.class);
        task.source(project.fileTree(docs.toFile(), spec -> spec.include("**/*.docx", "**/*.pptx", "**/*.xlsx")));
        task.getOutputDirectory().set(project.getLayout().getBuildDirectory().dir("ooxml/assets"));

        task.extract();

        Path assetsRoot = projectDir.toPath().resolve("build/ooxml/assets");
        long extractedFiles;
        try (java.util.stream.Stream<Path> stream = Files.walk(assetsRoot)) {
            extractedFiles = stream.filter(Files::isRegularFile).count();
        }
        assertTrue(extractedFiles == 0, "v1 benchmark corpus should not contain media/embedding assets");
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

