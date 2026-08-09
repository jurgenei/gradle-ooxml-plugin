package name.jurgenei.gradle.ooxml;

import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Normalizes task inputs from single-file and file-tree configuration styles.
 */
final class InputCollector {
    private InputCollector() {
    }

    /**
     * Resolves inputs in declaration order and validates that at least one source exists.
     *
     * @param inputFile optional direct input.
     * @param sourceFiles optional collection input.
     * @return ordered set of files to process.
     */
    static Set<File> resolve(RegularFileProperty inputFile, ConfigurableFileCollection sourceFiles) {
        LinkedHashSet<File> files = new LinkedHashSet<>();
        if (inputFile.isPresent()) {
            files.add(inputFile.get().getAsFile());
        }
        files.addAll(sourceFiles.getFiles());
        if (files.isEmpty()) {
            throw new GradleException("Configure either inputFile or source(fileTree(...)) for OOXML processing.");
        }
        return files;
    }
}

