package name.jurgenei.gradle.ooxml;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ExternalFdDocAuditTest {
    @Test
    void auditsExternalFdDocumentWhenPresent() throws Exception {
        Path input = Path.of("/Users/cs79en/Developer/Projects/lineage/work/src/fd/FD Cover Allocation v86.0.docx");
        org.junit.jupiter.api.Assumptions.assumeTrue(Files.exists(input), "External FD document not present.");

        Path output = Files.createTempFile("fd-cover-allocation-", ".canonical.xml");
        OoXmlCanonicalizer canonicalizer = new OoXmlCanonicalizer();
        CanonicalXmlSerializer serializer = new CanonicalXmlSerializer();
        serializer.write(canonicalizer.canonicalize(input.toFile()), output);

        String xml = Files.readString(output);
        DiagramAuditRunner.Report report = DiagramAuditRunner.inspect(xml);

        System.out.println("FD audit canonical: " + output.toAbsolutePath());
        System.out.println("FD audit graphs: " + report.graphs() + ", suspicious-singletons: " + report.suspiciousSingletons());
        report.suspiciousGraphs().forEach(suspiciousGraph ->
                System.out.println("FD suspicious source-path: " + suspiciousGraph.sourcePath()
                        + " tokens=" + suspiciousGraph.assetTextTokens()
                        + " preview=" + suspiciousGraph.preview()));

        assertTrue(report.graphs() > 0, "Expected at least one graph in FD document.");
    }
}

