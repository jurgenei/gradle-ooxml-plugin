package name.jurgenei.gradle.ooxml;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves canonical document id and version from input filename stem.
 */
final class VersionResolver {
    private static final Pattern V_PREFIX_PATTERN = Pattern.compile("^(?<id>.+?)_v(?<version>[0-9]+(?:\\.[0-9]+)*)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern NUMERIC_SUFFIX_PATTERN = Pattern.compile("^(?<id>.+?)_(?<version>[0-9]+(?:\\.[0-9]+)*)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern QUALIFIER_SUFFIX_PATTERN = Pattern.compile("^(?<id>.+?)_(?<version>FINAL|DRAFT|SNAPSHOT|RC[0-9]*)$", Pattern.CASE_INSENSITIVE);

    private VersionResolver() {
    }

    /**
     * Resolves known version suffix conventions.
     *
     * <p>Supported patterns include:</p>
     * <ul>
     *   <li>{@code name_v3}</li>
     *   <li>{@code name_1.2}</li>
     *   <li>{@code name_FINAL}, {@code name_DRAFT}, {@code name_SNAPSHOT}, {@code name_RC1}</li>
     * </ul>
     *
     * @param fileStem filename without extension.
     * @return resolved id/version tuple.
     */
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

    /**
     * Parsed identity/version pair used in canonical metadata.
     */
    record ResolvedVersion(String documentId, String version) { }
}

