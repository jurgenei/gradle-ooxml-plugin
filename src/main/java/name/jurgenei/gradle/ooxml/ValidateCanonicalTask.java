package name.jurgenei.gradle.ooxml;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Optional;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@DisableCachingByDefault(because = "Validation work depends on dynamic file content")
public abstract class ValidateCanonicalTask extends DefaultTask {
    @Inject
    public ValidateCanonicalTask() {
    }

    @Optional
    @InputDirectory
    public abstract DirectoryProperty getInputDirectory();

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
                files.filter(path -> path.toString().endsWith(".xml"))
                        .forEach(path -> validateFile(validator, path));
            }
        } catch (IOException | SAXException e) {
            throw new GradleException("Failed to validate canonical XML files", e);
        }
    }

    private Schema loadSchema() throws IOException, SAXException {
        try (InputStream input = getClass().getResourceAsStream("/schema/canonical.xsd")) {
            if (input == null) {
                throw new IOException("Missing schema resource: /schema/canonical.xsd");
            }
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            return schemaFactory.newSchema(new StreamSource(input));
        }
    }

    private void validateFile(Validator validator, Path xmlFile) {
        try (InputStream input = Files.newInputStream(xmlFile)) {
            validator.validate(new StreamSource(input));
        } catch (Exception e) {
            throw new GradleException("Canonical XML does not validate: " + xmlFile, e);
        }
    }
}

