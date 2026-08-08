package name.jurgenei.gradle.ooxml;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class VersionResolver {
    private static final Pattern V_PREFIX_PATTERN = Pattern.compile("^(?<id>.+?)_v(?<version>[0-9]+(?:\\.[0-9]+)*)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern NUMERIC_SUFFIX_PATTERN = Pattern.compile("^(?<id>.+?)_(?<version>[0-9]+(?:\\.[0-9]+)*)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern QUALIFIER_SUFFIX_PATTERN = Pattern.compile("^(?<id>.+?)_(?<version>FINAL|DRAFT|SNAPSHOT|RC[0-9]*)$", Pattern.CASE_INSENSITIVE);

    private VersionResolver() {
    }

    static ResolvedVersion resolve(String fileStem) {
        Matcher vPrefix = V_PREFIX_PATTERN.matcher(fileStem);
        if (vPrefix.matches()) {
            return new ResolvedVersion(vPrefix.group("id"), vPrefix.group("version"));
        }

        Matcher numericSuffix = NUMERIC_SUFFIX_PATTERN.matcher(fileStem);
        if (numericSuffix.matches()) {
            return new ResolvedVersion(numericSuffix.group("id"), numericSuffix.group("version"));
        }

        Matcher qualifierSuffix = QUALIFIER_SUFFIX_PATTERN.matcher(fileStem);
        if (qualifierSuffix.matches()) {
            return new ResolvedVersion(qualifierSuffix.group("id"), qualifierSuffix.group("version"));
        }

        return new ResolvedVersion(fileStem, "");
    }

    record ResolvedVersion(String documentId, String version) {
    }
}

