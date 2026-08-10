# gradle-ooxml-plugin

Gradle plugin that converts Office Open XML documents (`.docx`, `.pptx`, `.xlsx`) to canonical XML using docx4j and JAXB.

The plugin is intentionally focused on canonicalization and package asset extraction.

## What It Produces

Current canonical output includes:

- Paragraphs, lists, and tables
- Links and references
- Diagram topology (shapes and connectors)
- Provenance attributes (`source-document`, `source-path`)

## Benchmark v1

The repository includes a compact benchmark corpus for canonicalization regression testing:

- `src/test/resources/ooxml/v1-benchmark.docx`
- `src/test/resources/ooxml/v1-benchmark.pptx`
- `src/test/resources/ooxml/v1-benchmark.xlsx`

Specification and acceptance criteria:

- `ooxml-canonical-benchmark-spec-v1.md`

Representative canonical samples:

- `samples/ooxml-canonical-benchmark-v1/v1-benchmark.docx.sample.xml`
- `samples/ooxml-canonical-benchmark-v1/v1-benchmark.pptx.sample.xml`
- `samples/ooxml-canonical-benchmark-v1/v1-benchmark.xlsx.sample.xml`

### Benchmark coverage highlights

- DOCX: headings/paragraphs, ordered + unordered lists, 2x2 table, diagram text marker (`[A] -> [B]`)
- PPTX: slide text, shape + connector topology, 2x2 table
- XLSX: inline-string cells, sparse matrix coordinates, named range, merged-cell range metadata

### Determinism checks

`OoXmlCanonicalizerTest` runs each benchmark fixture more than once and verifies identical serialized XML.

## Plugin ID

- `name.jurgenei.gradle.ooxml`

## Extension

- `ooxml.canonicalSchemaUrl`
  - Readable URL (`file:`, `jar:`, etc.) to `canonical.xsd` for cross-plugin usage.
  - Intended for consumers such as `gradle-xml-plugin` bootstrap tasks.

## Responsibility Boundary

`gradle-ooxml-plugin` is responsible for:

- OOXML package validation
- Canonical XML emission
- Asset extraction from OOXML containers

`gradle-ooxml-plugin` is not responsible for:

- Business semantics
- Rule authoring
- Schematron/XSD policy management

Those belong to downstream tooling (for example `gradle-xml-plugin`).

## Tasks

- `ooxmlToCanonical` (`name.jurgenei.gradle.ooxml.OoXmlToCanonicalTask`)
  - Converts OOXML documents into canonical XML files.
- `extractAssets` (`name.jurgenei.gradle.ooxml.ExtractAssetsTask`)
  - Extracts media and embedded assets from OOXML packages.
- `validateCanonical` (`name.jurgenei.gradle.ooxml.ValidateCanonicalTask`)
  - Validates generated canonical XML against `canonical.xsd`.

### Task Inputs and Outputs

- `ooxmlToCanonical`
  - Inputs: `inputFile` or `source(fileTree(...))`
  - Output: `outputDirectory` with one canonical XML per source file
- `extractAssets`
  - Inputs: `inputFile` or `source(fileTree(...))`
  - Output: `outputDirectory/<document-stem>/...` copied OOXML media/embeddings
- `validateCanonical`
  - Input: `inputDirectory`
  - Output: validation success/failure (throws on invalid XML)

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

tasks.named('validateCanonical', name.jurgenei.gradle.ooxml.ValidateCanonicalTask) {
    inputDirectory.set(layout.buildDirectory.dir('ooxml/canonical'))
}
```

## Output Conventions

- Canonical XML files are named from input stem (for example `sample_v3.docx` -> `sample_v3.xml`).
- Asset extraction uses `outputDirectory/<input-stem>/...` preserving package-relative paths.

## Interop With `gradle-xml-plugin`

You can pass canonical schema location to `gradle-xml-plugin` tasks using:

- `ooxml.canonicalSchemaUrl` for URL-based consumption
- optional local copy strategy if you maintain curated rule/XSD pairs

Sample project (kept on OOXML side to preserve dependency direction):

- `samples/schematron-bootstrap-xml`
- Runs bootstrap + Schematron validation using `gradle-xml-plugin` while sourcing schema URL from `ooxml` extension

## Development

Run tests:

```bash
./gradlew test
```

Run only canonical benchmark tests:

```bash
./gradlew test --tests name.jurgenei.gradle.ooxml.OoXmlCanonicalizerTest
```

Run only task-level conversion benchmark coverage:

```bash
./gradlew test --tests name.jurgenei.gradle.ooxml.OoXmlToCanonicalTaskTest
```

Generate schema-first JAXB sources:

```bash
./gradlew generateCanonicalJaxb
```

## Test Layout

- Unit extraction tests: `src/test/java/name/jurgenei/gradle/ooxml/OoXmlCanonicalizerTest.java`
- Task conversion tests: `src/test/java/name/jurgenei/gradle/ooxml/OoXmlToCanonicalTaskTest.java`
- Functional Gradle TestKit tests: `src/test/java/name/jurgenei/gradle/ooxml/OoXmlPluginFunctionalTest.java`
- Asset extraction tests: `src/test/java/name/jurgenei/gradle/ooxml/ExtractAssetsTaskTest.java`

## Known v1 Scope Limits

- DOCX heading hierarchy is currently preserved as ordered paragraph content rather than explicit section nodes.
- PPTX connector endpoint IDs may be unavailable depending on source connector metadata.
- XLSX merged-cell semantics are emitted as references (`target=A4:B4`, `text=merge`) in v1.

