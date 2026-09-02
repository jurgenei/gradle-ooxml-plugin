package name.jurgenei.gradle.ooxml;

import name.jurgenei.gradle.ooxml.recognizer.AssetRecognizer;
import name.jurgenei.gradle.ooxml.recognizer.RecognizerRegistry;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.Input;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Converts OOXML inputs into canonical XML documents.
 */
@DisableCachingByDefault(because = "Output fan-out depends on input collection shape")
public abstract class OoXmlToCanonicalTask extends DefaultTask {
    private final OpenXmlValidator validator = new OpenXmlValidator();
    private final CanonicalXmlSerializer serializer = new CanonicalXmlSerializer();
    private final CanonicalZipPackageWriter packageWriter = new CanonicalZipPackageWriter();

    @Inject
    public OoXmlToCanonicalTask() {
        getLegacyXmlOutput().convention(false);
        getRecognizerClassNames().convention(List.of());
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

    @Input
    public abstract Property<Boolean> getLegacyXmlOutput();

    @Input
    public abstract ListProperty<String> getRecognizerClassNames();

    /**
     * Adds one or more sources using Gradle file notation.
     *
     * @param source file, folder, fileTree, or collection.
     */
    public void source(Object source) {
        getSourceFiles().from(source);
    }

    /**
     * Runs canonicalization for all resolved OOXML sources.
     */
    @TaskAction
    public void convert() {
        Path outputRoot = getOutputDirectory().get().getAsFile().toPath();
        Set<File> inputs = InputCollector.resolve(getInputFile(), getSourceFiles());
        OoXmlCanonicalizer canonicalizer = new OoXmlCanonicalizer(buildRegistry());
        try {
            Files.createDirectories(outputRoot);
            getLogger().debug("Preparing OOXML to canonical conversion for {} input file(s)", inputs.size());
            for (File input : inputs) {
                validator.validate(input);
                Path output = outputRoot.resolve(toOutputName(input));
                getLogger().debug("Converting '{}' to '{}'", input.getAbsolutePath(), output.toAbsolutePath());
                if (getLegacyXmlOutput().getOrElse(false)) {
                    serializer.write(canonicalizer.canonicalize(input), output);
                } else {
                    packageWriter.write(canonicalizer.canonicalize(input), input, output);
                }
            }
        } catch (Exception e) {
            throw new GradleException("Failed to convert OOXML to canonical XML", e);
        }
    }

    private RecognizerRegistry buildRegistry() {
        List<AssetRecognizer> additional = new ArrayList<>();
        for (String className : getRecognizerClassNames().getOrElse(List.of())) {
            additional.add(instantiateRecognizer(className));
        }
        return RecognizerRegistry.defaultRegistry(additional);
    }

    private AssetRecognizer instantiateRecognizer(String className) {
        try {
            Class<?> type = Class.forName(className);
            if (!AssetRecognizer.class.isAssignableFrom(type)) {
                throw new GradleException("Configured recognizer does not implement AssetRecognizer: " + className);
            }
            return (AssetRecognizer) type.getDeclaredConstructor().newInstance();
        } catch (GradleException e) {
            throw e;
        } catch (Exception e) {
            throw new GradleException("Failed to instantiate recognizer: " + className, e);
        }
    }

    private String toOutputName(File input) throws IOException {
        String name = input.getName();
        int dot = name.lastIndexOf('.');
        String stem = dot > 0 ? name.substring(0, dot) : name;
        if (getLegacyXmlOutput().getOrElse(false)) {
            return stem + ".xml";
        }
        String extension = dot > 0 ? name.substring(dot + 1).toLowerCase() : "ooxml";
        return stem + "_" + extension + ".zip";
    }
}
