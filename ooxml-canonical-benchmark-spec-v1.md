# OOXML Canonical Benchmark Spec v1

## Objective

This benchmark verifies the canonical extraction paths for a deliberately small corpus:

- `v1-benchmark.docx`
- `v1-benchmark.pptx`
- `v1-benchmark.xlsx`

The corpus is designed for fast unit tests and deterministic regression checks.

## Corpus Summary

### `v1-benchmark.docx`

- H1 and H2 headings
- Paragraphs
- Numbered list and bullet list
- 2x2 table
- Simple diagram text representation: `[A] -> [B]`

### `v1-benchmark.pptx`

- Slide 1: title + body text
- Slide 2: two shapes and one connector (`CRM` -> `SAP`)
- Slide 3: 2x2 table

### `v1-benchmark.xlsx`

- Worksheet with tabular data
- Worksheet with sparse matrix-style data
- Worksheet with defined name and merged cell

## Canonical Output Contract (v1)

### General requirements

1. Canonicalization is deterministic across repeated runs.
2. Document order is preserved.
3. Rich structures are emitted into structural elements when supported.
4. Provenance attributes are populated where available (`source-path`).

### DOCX requirements

1. Heading and paragraph content is captured as `Paragraph` entries in source order.
2. Heading paragraphs are labeled (`label="h1"`, `label="h2"`) for benchmark heading styles.
3. Numbered list items are emitted as `List ordered="true"`.
4. Bullet list items are emitted as `List ordered="false"`.
5. The 2x2 table is emitted as `Table -> Row -> Cell`.
6. Diagram text `[A] -> [B]` is preserved in paragraph content.

Expected benchmark signals:

- `Benchmark Document`
- `Section A`
- `Section B`
- `First item`, `Second item`
- `Alpha`, `Beta`
- `App`, `Team`, `CRM`, `Sales`
- `[A] -> [B]`

### PPTX requirements

1. Slide text is emitted as `Paragraph` entries with slide provenance paths.
2. Connector diagrams are emitted as `Diagram` with `Shape` and `Connector` entries.
3. Slide tables are emitted as canonical table rows/cells.
4. Body-level `Table` and `Diagram` elements include `source-path` provenance.

Expected benchmark signals:

- `Overview`
- `System landscape overview`
- `Architecture`
- `Responsibilities`
- Diagram shape labels including `CRM`, `SAP`
- Connector element present

### XLSX requirements

1. Inline string cells are extracted (`inlineStr`) and emitted in table and paragraph forms.
2. Sparse matrix coordinates remain stable in provenance (`/xl/worksheets/.../<cellRef>`).
3. Workbook defined names are emitted as `Reference` entries (`target=range`, `text=name`).
4. Merge ranges are emitted as `Reference` entries with merge metadata (`text=merge`).

Expected benchmark signals:

- table cells include `Application`, `EU`, `Merged Cell`
- `NamedRange!A1:B2` (named range target)
- `A4:B4` (merge range)

## Test Implementation Map

- Unit tests: `src/test/java/name/jurgenei/gradle/ooxml/OoXmlCanonicalizerTest.java`
- Task-level conversion coverage: `src/test/java/name/jurgenei/gradle/ooxml/OoXmlToCanonicalTaskTest.java`
- Functional task wiring coverage: `src/test/java/name/jurgenei/gradle/ooxml/OoXmlPluginFunctionalTest.java`
- Sample canonical outputs: `samples/ooxml-canonical-benchmark-v1/`

## Pass Criteria

A benchmark run passes when:

1. All three fixtures canonicalize without errors.
2. Required structure signals per format are present.
3. XLSX defined names and merge references are preserved.
4. Repeated canonicalization of each file produces byte-identical XML.

## Recommended Future Extensions

- DOCX hyperlinks, embedded drawings, footnotes
- PPTX explicit connector endpoint mapping
- XLSX chart sheets, formulas, and native table metadata
