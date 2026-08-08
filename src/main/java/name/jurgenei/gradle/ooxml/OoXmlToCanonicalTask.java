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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

@DisableCachingByDefault(because = "Output fan-out depends on input collection shape")
public abstract class OoXmlToCanonicalTask extends DefaultTask {
    private final OpenXmlValidator validator = new OpenXmlValidator();
    private final OoXmlCanonicalizer canonicalizer = new OoXmlCanonicalizer();
    private final CanonicalXmlSerializer serializer = new CanonicalXmlSerializer();

    @Inject
    public OoXmlToCanonicalTask() {
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
    public void convert() {
        Path outputRoot = getOutputDirectory().get().getAsFile().toPath();
        Set<File> inputs = InputCollector.resolve(getInputFile(), getSourceFiles());
        try {
            Files.createDirectories(outputRoot);
            for (File input : inputs) {
                validator.validate(input);
                Path output = outputRoot.resolve(toOutputName(input));
                serializer.write(canonicalizer.canonicalize(input), output);
            }
        } catch (Exception e) {
            throw new GradleException("Failed to convert OOXML to canonical XML", e);
        }
    }

    private String toOutputName(File input) throws IOException {
        String name = input.getName();
        int dot = name.lastIndexOf('.');
        String stem = dot > 0 ? name.substring(0, dot) : name;
        return stem + ".xml";
    }
}

