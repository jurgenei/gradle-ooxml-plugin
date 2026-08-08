package name.jurgenei.gradle.ooxml;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OoXmlPluginTest {
    @Test
    void registersExpectedTasks() {
        Project project = ProjectBuilder.builder().build();
        project.getPlugins().apply(OoXmlPlugin.class);

        assertNotNull(project.getTasks().findByName("ooxmlToCanonical"));
        assertNotNull(project.getTasks().findByName("extractAssets"));
        assertNotNull(project.getTasks().findByName("validateCanonical"));

        assertInstanceOf(OoXmlToCanonicalTask.class, project.getTasks().getByName("ooxmlToCanonical"));
        assertInstanceOf(ExtractAssetsTask.class, project.getTasks().getByName("extractAssets"));
        assertInstanceOf(ValidateCanonicalTask.class, project.getTasks().getByName("validateCanonical"));
    }
}

