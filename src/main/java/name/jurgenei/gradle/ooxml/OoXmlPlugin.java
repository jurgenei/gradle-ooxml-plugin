package name.jurgenei.gradle.ooxml;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;

import java.net.URL;

/**
 * Registers OOXML canonicalization tasks and extension metadata.
 */
public class OoXmlPlugin implements Plugin<Project> {
    /**
     * Applies the plugin and contributes tasks plus the {@code ooxml} extension.
     *
     * @param project target project.
     */
    @Override
    public void apply(Project project) {
        OoXmlExtension extension = project.getExtensions().create("ooxml", OoXmlExtension.class);
        extension.getCanonicalSchemaUrl().convention(project.provider(() -> resolveSchemaUrl(project)));

        TaskProvider<OoXmlToCanonicalTask> canonicalTask = project.getTasks().register("ooxmlToCanonical", OoXmlToCanonicalTask.class, task -> {
            task.setGroup("ooxml");
            task.setDescription("Converts DOCX/PPTX/XLSX files to canonical zip packages.");
            task.getOutputDirectory().convention(project.getLayout().getBuildDirectory().dir("ooxml/canonical"));
        });

        project.getTasks().register("extractAssets", ExtractAssetsTask.class, task -> {
            task.setGroup("ooxml");
            task.setDescription("Extracts media and embedded assets from DOCX/PPTX/XLSX files.");
            task.getOutputDirectory().convention(project.getLayout().getBuildDirectory().dir("ooxml/assets"));
        });

        project.getTasks().register("validateCanonical", ValidateCanonicalTask.class, task -> {
            task.setGroup("ooxml");
            task.setDescription("Validates canonical XML files against the canonical schema.");
            task.getInputDirectory().convention(project.getLayout().getBuildDirectory().dir("ooxml/canonical"));
            task.dependsOn(canonicalTask);
        });
    }

    /**
     * Resolves canonical schema location for external consumers.
     *
     * @param project target project.
     * @return URL string to canonical schema.
     */
    private String resolveSchemaUrl(Project project) {
        URL classpathSchema = OoXmlPlugin.class.getClassLoader().getResource("schema/canonical.xsd");
        if (classpathSchema != null) {
            return classpathSchema.toExternalForm();
        }
        return project.file("src/main/resources/schema/canonical.xsd").toURI().toString();
    }
}

