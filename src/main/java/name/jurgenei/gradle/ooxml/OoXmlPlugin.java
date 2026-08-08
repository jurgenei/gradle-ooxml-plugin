package name.jurgenei.gradle.ooxml;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;

public class OoXmlPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        TaskProvider<OoXmlToCanonicalTask> canonicalTask = project.getTasks().register("ooxmlToCanonical", OoXmlToCanonicalTask.class, task -> {
            task.setGroup("ooxml");
            task.setDescription("Converts DOCX/PPTX/XLSX files to canonical XML.");
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
}

