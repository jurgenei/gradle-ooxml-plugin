package name.jurgenei.gradle.ooxml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

final class AssetExtractor {
    void extract(File input, Path outputDirectory) throws IOException {
        String stem = stem(input.getName());
        Path base = outputDirectory.resolve(stem);
        try (ZipFile zipFile = new ZipFile(input)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }
                String normalized = normalize(entry.getName());
                if (isAsset(normalized)) {
                    Path target = base.resolve(normalized);
                    Files.createDirectories(target.getParent());
                    try (InputStream inputStream = zipFile.getInputStream(entry)) {
                        Files.copy(inputStream, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        }
    }

    private boolean isAsset(String path) {
        return path.startsWith("word/media/") || path.startsWith("ppt/media/") || path.startsWith("xl/media/")
                || path.startsWith("word/embeddings/") || path.startsWith("ppt/embeddings/") || path.startsWith("xl/embeddings/");
    }

    private String normalize(String path) {
        String normalized = path.replace('\\', '/');
        return normalized.startsWith("/") ? normalized.substring(1) : normalized;
    }

    private String stem(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }
}

