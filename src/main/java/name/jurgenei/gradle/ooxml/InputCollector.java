package name.jurgenei.gradle.ooxml;

import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

final class InputCollector {
    private InputCollector() {
    }

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

