package name.jurgenei.gradle.ooxml;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ValidateCanonicalTaskTest {
    @Test
    void validatesGeneratedCanonicalXml() throws Exception {
        File projectDir = Files.createTempDirectory("ooxml-validate-project").toFile();
        Project project = ProjectBuilder.builder().withProjectDir(projectDir).build();

        Path canonical = projectDir.toPath().resolve("build/ooxml/canonical");
        Files.createDirectories(canonical);
        Files.writeString(canonical.resolve("sample.xml"), """
                <c:Document xmlns:c=\"http://jurgenei.name/canonical\">
                  <c:Metadata>
                    <c:DocumentId>sample</c:DocumentId>
                    <c:Version>1</c:Version>
                    <c:SourceFile>sample.docx</c:SourceFile>
                    <c:DocumentType>DOCX</c:DocumentType>
                  </c:Metadata>
                  <c:Body>
                    <c:Paragraph>
                      <c:Text>Hello</c:Text>
                    </c:Paragraph>
                  </c:Body>
                </c:Document>
                """, StandardCharsets.UTF_8);

        ValidateCanonicalTask task = project.getTasks().create("validateCanonical", ValidateCanonicalTask.class);
        task.getInputDirectory().set(project.getLayout().getProjectDirectory().dir("build/ooxml/canonical"));

        assertDoesNotThrow(task::validate);
    }
}

