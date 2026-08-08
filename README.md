# gradle-ooxml-plugin

Gradle plugin that converts Office Open XML documents (`.docx`, `.pptx`, `.xlsx`) to canonical XML using docx4j and JAXB.

Current canonical output includes:

- Paragraphs, lists, and tables
- Links and references
- Diagram topology (shapes and connectors)

## Plugin ID

- `name.jurgenei.gradle.ooxml`

## Tasks

- `ooxmlToCanonical` (`name.jurgenei.gradle.ooxml.OoXmlToCanonicalTask`)
  - Converts OOXML documents into canonical XML files.
- `extractAssets` (`name.jurgenei.gradle.ooxml.ExtractAssetsTask`)
  - Extracts media and embedded assets from OOXML packages.
- `validateCanonical` (`name.jurgenei.gradle.ooxml.ValidateCanonicalTask`)
  - Validates generated canonical XML against `canonical.xsd`.

Build task:

- `generateCanonicalJaxb`
  - Generates JAXB classes from `src/main/resources/schema/canonical.xsd` into `name.jurgenei.gradle.ooxml.generated.canonical`.

## Quick Example

```groovy
plugins {
    id 'name.jurgenei.gradle.ooxml'
}

tasks.named('ooxmlToCanonical', name.jurgenei.gradle.ooxml.OoXmlToCanonicalTask) {
    source(fileTree(layout.projectDirectory.dir('docs')) {
        include '**/*.docx', '**/*.pptx', '**/*.xlsx'
    })
    outputDirectory.set(layout.buildDirectory.dir('ooxml/canonical'))
}

tasks.named('extractAssets', name.jurgenei.gradle.ooxml.ExtractAssetsTask) {
    source(fileTree(layout.projectDirectory.dir('docs')) {
        include '**/*.docx', '**/*.pptx', '**/*.xlsx'
    })
    outputDirectory.set(layout.buildDirectory.dir('ooxml/assets'))
}
```

## Development

Run tests:

```bash
./gradlew test
```

Generate schema-first JAXB sources:

```bash
./gradlew generateCanonicalJaxb
```

