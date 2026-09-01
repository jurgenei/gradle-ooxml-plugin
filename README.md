# gradle-ooxml-plugin

Gradle plugin that converts Office Open XML documents (`.docx`, `.pptx`, `.xlsx`) to canonical XML using docx4j and JAXB.

The plugin is intentionally focused on canonicalization and package asset extraction.

## What It Produces

Current canonical output includes:

- Paragraph (`para`), list (`list`), and table (`table`) structures
- Links and references
- Diagram topology (shapes and connectors)
- Provenance attribute (`source-path`) with optional paragraph labels (for example `h1`, `h2`)

Specification and acceptance criteria:

[ooxml-canonical-benchmark-spec-v1.md](ooxml-canonical-benchmark-spec-v1.md)

## What Canonicalization Means Here

Short version for non-technical readers: [CANONICALIZATION_EXECUTIVE_OVERVIEW.md](CANONICALIZATION_EXECUTIVE_OVERVIEW.md).

In this plugin, **canonicalization** means converting different OOXML formats (`.docx`, `.pptx`, `.xlsx`) into one stable, normalized representation so downstream tools can process all inputs in a consistent way.

For this project, canonicalization is not only text extraction. It means:

- preserving structure (`para`, `list`, `table`, `link`, `reference`, `graph`)
- preserving provenance (`source-path`) and media linkage (`Diagram@href`)
- producing deterministic output (same input -> same canonical shape)
- hiding OOXML package complexity behind one schema-driven contract (`canonical.xml`)

This lets validation and transformation services operate against one canonical schema instead of format-specific XML dialects.

## Why docx4j + Canonical Model First

`gradle-ooxml-plugin` prioritizes deterministic, schema-valid canonical output with provenance.
That requirement favors a controlled OOXML-to-canonical mapping layer.

Why this path was selected:

- `docx4j` provides strong OOXML package/model handling for XML-first extraction.
- the canonical model keeps behavior explicit, testable, and stable across releases.
- the output contract (`*_ext.zip` with `canonical.xml` + referenced `media/*`) is relationship-aware and deterministic.

Why not switch directly to Apache POI/Tika as the core engine:

- **Apache POI** is strong for Office object models (especially XLSX), but replacing the current foundation now would add migration risk and can destabilize canonical determinism.
- **Apache Tika** is strong for broad extraction/detection, but too coarse as the source of truth for strict structural canonicalization.

Current strategy:

- keep the canonical pipeline as the default foundation
- introduce POI/Tika only for narrow, high-value extraction gaps
- gate enrichments behind feature flags and deterministic regression tests before changing defaults

## Forward Path for Diagram and Asset Enrichment

### VSDX -> Visio XML

- parse VSDX package parts and map shapes/connectors/text into canonical `Diagram` nodes/edges
- retain provenance and package extracted artifacts into canonical zip media entries
- start with topology and labels; defer advanced layout semantics

### SVG -> DOM/Batik

- parse SVG through DOM/Batik for robust vector traversal
- map text, groups, paths, and marker-based connectors to canonical diagram structures
- keep mapping deterministic and confidence-scored where inference is required

### EMF -> FreeHEP

- decode EMF/WMF primitives and embedded text
- infer basic node/edge topology from vector instructions
- prioritize stable text + simple flow inference before advanced geometry modeling

### PNG -> OpenCV + Tesseract

- use OpenCV for preprocessing and segmentation (line/region detection)
- use Tesseract for OCR text extraction
- infer canonical graph hints with confidence scores; keep as fallback due to OCR variability

### Implementation Sequence

1. add an `AssetAnalyzer` extension point by media type
2. keep current extraction as baseline fallback
3. onboard one analyzer at a time with fixture-based deterministic tests
4. promote analyzers to default only after benchmark quality and stability thresholds are met

## Benchmark v1

compact benchmark corpus for canonicalization regression testing:

| Source Document                                                                          | Canonical result |
|------------------------------------------------------------------------------------------| ---------------- |
| [src/test/resources/ooxml/v1-benchmark.docx](src/test/resources/ooxml/v1-benchmark.docx) | [samples/ooxml-canonical-benchmark-v1/v1-benchmark.docx.sample.xml](samples/ooxml-canonical-benchmark-v1/v1-benchmark.docx.sample.xml) |
| [src/test/resources/ooxml/v1-benchmark.pptx](src/test/resources/ooxml/v1-benchmark.pptx) | [samples/ooxml-canonical-benchmark-v1/v1-benchmark.pptx.sample.xml](samples/ooxml-canonical-benchmark-v1/v1-benchmark.pptx.sample.xml) |
| [src/test/resources/ooxml/v1-benchmark.xlsx](src/test/resources/ooxml/v1-benchmark.xlsx) | [samples/ooxml-canonical-benchmark-v1/v1-benchmark.xlsx.sample.xml](samples/ooxml-canonical-benchmark-v1/v1-benchmark.xlsx.sample.xml) |

## Benchmark v2

Formulas and diagram topology benchmark corpus for canonicalization regression testing:

| Source Document                                                                        | Canonical result |
|----------------------------------------------------------------------------------------| ---------------- |
| [src/test/resources/ooxml/v2-formulas.docx](src/test/resources/ooxml/v2-formulas.docx) | [samples/ooxml-canonical-benchmark-v2/v2-formulas.xml](samples/ooxml-canonical-benchmark-v2/v2-formulas.xml) |
| [src/test/resources/ooxml/v2-diagrams.docx](src/test/resources/ooxml/v2-diagrams.docx) | [samples/ooxml-canonical-benchmark-v2/v2-diagrams.xml](samples/ooxml-canonical-benchmark-v2/v2-diagrams.xml) |

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
  - Converts OOXML documents into canonical zip packages (`canonical.xml` + `media/*`).
- `extractAssets` (`name.jurgenei.gradle.ooxml.ExtractAssetsTask`)
  - Extracts media and embedded assets from OOXML packages.
- `validateCanonical` (`name.jurgenei.gradle.ooxml.ValidateCanonicalTask`)
  - Validates generated canonical XML against `canonical.xsd` (standalone `.xml` or `canonical.xml` inside `.zip`).

### Task Inputs and Outputs

- `ooxmlToCanonical`
  - Inputs: `inputFile` or `source(fileTree(...))`
  - Optional input: `legacyXmlOutput` (default `false`) to emit historical flat `.xml` files instead of zip packages
  - Output: `outputDirectory` with one canonical zip per source file
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

Canonical XML element naming:

- All canonical namespace element names are lowercase.
- Paragraph element name is `para` (short form).
- Core shape: `document -> metadata + body -> para/list/table/link/reference (+ graphml graph)`.

- Canonical packages are named from input stem + extension (for example `document.docx` -> `document_docx.zip`).
- Each package contains:
  - `canonical.xml`
  - `media/<asset-file>` entries referenced by canonical `Diagram@href` values.
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

