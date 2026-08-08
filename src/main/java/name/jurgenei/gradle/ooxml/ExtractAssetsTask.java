package name.jurgenei.gradle.ooxml;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

@DisableCachingByDefault(because = "Output depends on package internals")
public abstract class ExtractAssetsTask extends DefaultTask {
    private final OpenXmlValidator validator = new OpenXmlValidator();
    private final AssetExtractor assetExtractor = new AssetExtractor();

    @Inject
    public ExtractAssetsTask() {
    }

    @Optional
    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract RegularFileProperty getInputFile();

    @Optional
    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract ConfigurableFileCollection getSourceFiles();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    public void source(Object source) {
        getSourceFiles().from(source);
    }

    @TaskAction
    public void extract() {
        Path outputRoot = getOutputDirectory().get().getAsFile().toPath();
        Set<File> inputs = InputCollector.resolve(getInputFile(), getSourceFiles());
        try {
            Files.createDirectories(outputRoot);
            for (File input : inputs) {
                validator.validate(input);
                assetExtractor.extract(input, outputRoot);
            }
        } catch (Exception e) {
            throw new GradleException("Failed to extract OOXML assets", e);
        }
    }
}

