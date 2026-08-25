package name.jurgenei.gradle.ooxml;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VersionResolverTest {
    @Test
    void resolvesDashedVersionPrefixPattern() {
        VersionResolver.ResolvedVersion resolved = VersionResolver.resolve("v2-diagrams");

        assertEquals("v2-diagrams", resolved.documentId());
        assertEquals("v2", resolved.version());
    }

    @Test
    void resolvesVPrefixVersionPattern() {
        VersionResolver.ResolvedVersion resolved = VersionResolver.resolve("CustomerData_v3");

        assertEquals("CustomerData", resolved.documentId());
        assertEquals("3", resolved.version());
    }

    @Test
    void resolvesNumericSuffixPattern() {
        VersionResolver.ResolvedVersion resolved = VersionResolver.resolve("CustomerData_1.2");

        assertEquals("CustomerData", resolved.documentId());
        assertEquals("1.2", resolved.version());
    }

    @Test
    void resolvesQualifierSuffixPattern() {
        VersionResolver.ResolvedVersion resolved = VersionResolver.resolve("CustomerData_FINAL");

        assertEquals("CustomerData", resolved.documentId());
        assertEquals("FINAL", resolved.version());
    }

    @Test
    void fallsBackWhenPatternIsUnknown() {
        VersionResolver.ResolvedVersion resolved = VersionResolver.resolve("Customer_Data_Model");

        assertEquals("Customer_Data_Model", resolved.documentId());
        assertEquals("", resolved.version());
    }
}

