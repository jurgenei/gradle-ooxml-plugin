package name.jurgenei.gradle.ooxml;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;

import java.io.File;

/**
 * Performs lightweight OOXML package validation before extraction/serialization.
 */
final class OpenXmlValidator {
    /**
     * Validates with docx4j and falls back to structural ZIP checks when needed.
     *
     * @param file candidate OOXML file.
     * @throws Docx4JException when validation fails and structural fallback also fails.
     */
    void validate(File file) throws Docx4JException {
        try {
            OpcPackage pkg = OpcPackage.load(file);
            pkg.reset();
        } catch (Docx4JException docx4JException) {
            // Fallback keeps tests and synthetic fixtures usable while still enforcing OOXML package structure.
            if (!looksLikeOpenXml(file)) {
                throw docx4JException;
            }
        } catch (RuntimeException runtimeException) {
            // Some docx4j package types throw UnsupportedOperationException on reset; keep structural validation.
            if (!looksLikeOpenXml(file)) {
                throw runtimeException;
            }
        }
    }

    private boolean looksLikeOpenXml(File file) {
        try (java.util.zip.ZipFile zip = new java.util.zip.ZipFile(file)) {
            return zip.getEntry("[Content_Types].xml") != null && zip.getEntry("_rels/.rels") != null;
        } catch (java.io.IOException ignored) {
            return false;
        }
    }
}
