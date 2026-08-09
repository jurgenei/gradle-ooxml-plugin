package name.jurgenei.gradle.ooxml;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

/**
 * Public extension exposing canonical schema metadata for cross-plugin integration.
 */
public abstract class OoXmlExtension {

    @Inject
    public OoXmlExtension(ObjectFactory objects) {
        // Constructor kept for Gradle managed instantiation.
    }

    /**
     * URL to canonical.xsd (file:, jar:, etc.) for consumers such as gradle-xml-plugin.
     *
     * @return readable schema URL string.
     */
    public abstract Property<String> getCanonicalSchemaUrl();
}

