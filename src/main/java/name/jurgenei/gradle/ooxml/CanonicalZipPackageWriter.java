package name.jurgenei.gradle.ooxml;

import name.jurgenei.gradle.ooxml.canonical.CanonicalDocument;
import name.jurgenei.gradle.ooxml.canonical.Diagram;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Writes canonical output as a zip package with canonical XML plus extracted media.
 */
final class CanonicalZipPackageWriter {
    private final CanonicalXmlSerializer serializer = new CanonicalXmlSerializer();

    void write(CanonicalDocument document, File sourceOoxml, Path outputZip) throws IOException {
        Files.createDirectories(outputZip.getParent());
        Set<String> referencedMediaNames = referencedMediaNames(document);
        Map<String, byte[]> mediaEntries = readMediaEntries(sourceOoxml, referencedMediaNames);

        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(outputZip))) {
            zip.putNextEntry(new ZipEntry("canonical.xml"));
            try {
                serializer.write(document, zip);
            } catch (jakarta.xml.bind.JAXBException e) {
                throw new IOException("Failed to serialize canonical XML", e);
            }
            zip.closeEntry();

            if (!mediaEntries.isEmpty()) {
                zip.putNextEntry(new ZipEntry("media/"));
                zip.closeEntry();
            }

            List<String> names = new ArrayList<>(mediaEntries.keySet());
            names.sort(Comparator.naturalOrder());
            for (String name : names) {
                zip.putNextEntry(new ZipEntry("media/" + name));
                zip.write(mediaEntries.get(name));
                zip.closeEntry();
            }
        }
    }

    private Map<String, byte[]> readMediaEntries(File sourceOoxml, Set<String> referencedMediaNames) throws IOException {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        try (ZipFile zipFile = new ZipFile(sourceOoxml)) {
            Enumeration<? extends ZipEntry> sourceEntries = zipFile.entries();
            while (sourceEntries.hasMoreElements()) {
                ZipEntry entry = sourceEntries.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }
                String normalized = normalize(entry.getName());
                if (!isMedia(normalized)) {
                    continue;
                }
                String fileName = fileName(normalized);
                if (fileName.isEmpty() || entries.containsKey(fileName) || !referencedMediaNames.contains(fileName)) {
                    continue;
                }
                try (InputStream input = zipFile.getInputStream(entry)) {
                    entries.put(fileName, input.readAllBytes());
                }
            }
        }
        return entries;
    }

    private Set<String> referencedMediaNames(CanonicalDocument document) {
        Set<String> names = new LinkedHashSet<>();
        if (document == null || document.getBody() == null) {
            return names;
        }
        for (Diagram diagram : document.getBody().getDiagrams()) {
            String href = diagram.getHref();
            if (href == null || href.isBlank()) {
                continue;
            }
            String fileName = fileName(normalize(href));
            if (!fileName.isEmpty()) {
                names.add(fileName);
            }
        }
        return names;
    }

    private boolean isMedia(String path) {
        return path.startsWith("word/media/") || path.startsWith("ppt/media/") || path.startsWith("xl/media/");
    }

    private String normalize(String path) {
        String normalized = path.replace('\\', '/');
        return normalized.startsWith("/") ? normalized.substring(1) : normalized;
    }

    private String fileName(String path) {
        int slash = path.lastIndexOf('/');
        return slash >= 0 ? path.substring(slash + 1) : path;
    }
}

