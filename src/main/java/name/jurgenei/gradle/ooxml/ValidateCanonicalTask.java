package name.jurgenei.gradle.ooxml;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Validates canonical XML documents against bundled {@code canonical.xsd}.
 */
@DisableCachingByDefault(because = "Validation work depends on dynamic file content")
public abstract class ValidateCanonicalTask extends DefaultTask {
    @Inject
    public ValidateCanonicalTask() {
    }

    @Optional
    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getInputDirectory();

    /**
     * Validates every XML file found under {@link #getInputDirectory()}.
     */
    @TaskAction
    public void validate() {
        if (!getInputDirectory().isPresent()) {
            throw new GradleException("Input directory is not configured for canonical validation");
        }

        Path inputDir = getInputDirectory().get().getAsFile().toPath();
        if (!Files.exists(inputDir)) {
            throw new GradleException("Canonical input directory does not exist: " + inputDir);
        }

        try {
            Schema schema = loadSchema();
            Validator validator = schema.newValidator();
            try (Stream<Path> files = Files.walk(inputDir)) {
                files.filter(Files::isRegularFile)
                        .forEach(path -> validatePath(validator, path));
            }
        } catch (IOException | SAXException e) {
            throw new GradleException("Failed to validate canonical XML files", e);
        }
    }

    private void validatePath(Validator validator, Path path) {
        String fileName = path.getFileName().toString().toLowerCase();
        if (fileName.endsWith(".xml")) {
            validateFile(validator, path);
            return;
        }
        if (fileName.endsWith(".zip")) {
            validateZipCanonical(validator, path);
        }
    }

    private Schema loadSchema() throws IOException, SAXException {
        URL schemaUrl = getClass().getResource("/schema/canonical.xsd");
        if (schemaUrl == null) {
            throw new IOException("Missing schema resource: /schema/canonical.xsd");
        }
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        return schemaFactory.newSchema(schemaUrl);
    }

    private void validateFile(Validator validator, Path xmlFile) {
        try (InputStream input = Files.newInputStream(xmlFile)) {
            validator.validate(new StreamSource(input));
        } catch (Exception e) {
            throw new GradleException("Canonical XML does not validate: " + xmlFile, e);
        }
    }

    private void validateZipCanonical(Validator validator, Path zipFilePath) {
        try (ZipFile zipFile = new ZipFile(zipFilePath.toFile())) {
            ZipEntry canonical = zipFile.getEntry("canonical.xml");
            if (canonical == null) {
                throw new GradleException("Missing canonical.xml in package: " + zipFilePath);
            }
            try (InputStream input = zipFile.getInputStream(canonical)) {
                validator.validate(new StreamSource(input));
            }
        } catch (GradleException e) {
            throw e;
        } catch (Exception e) {
            throw new GradleException("Canonical package does not validate: " + zipFilePath, e);
        }
    }
}

