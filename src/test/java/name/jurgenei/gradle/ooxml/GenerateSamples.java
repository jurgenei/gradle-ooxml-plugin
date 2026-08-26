package name.jurgenei.gradle.ooxml;

import name.jurgenei.gradle.ooxml.canonical.CanonicalDocument;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility to regenerate sample XML files from benchmark fixtures.
 */
class GenerateSamples {

    @Test
    void regenerateSampleXmlFiles() throws Exception {
        OoXmlCanonicalizer canonicalizer = new OoXmlCanonicalizer();
        CanonicalXmlSerializer serializer = new CanonicalXmlSerializer();

        Path samplesDir = Paths.get("samples/ooxml-canonical-benchmark-v1");
        Path samplesV2Dir = Paths.get("samples/ooxml-canonical-benchmark-v2");

        // Generate docx sample
        generateSample(canonicalizer, serializer, "v1-benchmark.docx", samplesDir.resolve("v1-benchmark.docx.sample.xml"));

        // Generate pptx sample
        generateSample(canonicalizer, serializer, "v1-benchmark.pptx", samplesDir.resolve("v1-benchmark.pptx.sample.xml"));

        // Generate xlsx sample
        generateSample(canonicalizer, serializer, "v1-benchmark.xlsx", samplesDir.resolve("v1-benchmark.xlsx.sample.xml"));

        // Generate v2 diagrams sample
        generateSample(canonicalizer, serializer, "v2-diagrams.docx", samplesV2Dir.resolve("v2-diagrams.xml"));

        System.out.println("Sample XML files generated successfully!");
    }

    private void generateSample(OoXmlCanonicalizer canonicalizer, CanonicalXmlSerializer serializer,
                                String fixtureName, Path outputPath) throws Exception {
        System.out.println("Generating sample for: " + fixtureName);

        // Copy fixture to temp location
        Path tempDir = Files.createTempDirectory("ooxml-fixture-");
        Path temp = tempDir.resolve(fixtureName);
        try (InputStream input = getClass().getResourceAsStream("/ooxml/" + fixtureName)) {
            if (input == null) {
                throw new IllegalStateException("Missing fixture: " + fixtureName);
            }
            Files.copy(input, temp, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }

        // Canonicalize
        CanonicalDocument document = canonicalizer.canonicalize(temp.toFile());

        // Write to output
        serializer.write(document, outputPath);

        System.out.println("  -> Written to: " + outputPath);
    }
}

