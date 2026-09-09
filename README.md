# gradle-ooxml-plugin

![Conformance](https://img.shields.io/badge/Conformance-Check--All%20Passing-brightgreen)
[![Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/name.jurgenei.gradle.ooxml?label=Plugin%20Portal)](https://plugins.gradle.org/plugin/name.jurgenei.gradle.ooxml)
[![Build and Test](https://github.com/jurgenei/gradle-ooxml-plugin/actions/workflows/gradle-build.yml/badge.svg)](https://github.com/jurgenei/gradle-ooxml-plugin/actions/workflows/gradle-build.yml)
[![Coverage CI](https://github.com/jurgenei/gradle-ooxml-plugin/actions/workflows/coverage.yml/badge.svg)](https://github.com/jurgenei/gradle-ooxml-plugin/actions/workflows/coverage.yml)
[![codecov](https://codecov.io/gh/jurgenei/gradle-ooxml-plugin/graph/badge.svg?token=H9YDrwr94Y)](https://codecov.io/gh/jurgenei/gradle-ooxml-plugin)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/java-21+-green.svg)](https://www.oracle.com/java/)
[![Gradle](https://img.shields.io/badge/gradle-8+-blue.svg)](https://gradle.org/)

Gradle plugin that converts Office Open XML documents (`.docx`, `.pptx`, `.xlsx`) into canonical XML using `docx4j` + `JAXB`.

Plugin focus: deterministic canonicalisation and package asset extraction.

## Why Canonicalise Before LLM

Office files carry useful meaning inside complex packaging and dialects:

- WordprocessingML
- PresentationML
- SpreadsheetML
- DrawingML
- relationship parts

LLMs perform better when input is clean, ordered, and typed.

Canonicalisation gives this:

- stable structure (`para`, `list`, `table`, `reference`, `chart`, GraphML `graph`)
- stable provenance (`source-path`, media `href`)
- deterministic serialisation (same input -> same canonical output)
- format-neutral contract (`canonical.xml`)

Result: preprocessing reduces ambiguity, improves retrieval quality, and controls token spend by removing OOXML noise.

## Deterministic Boundary vs LLM Boundary

This plugin handles **syntax and structural semantics** with deterministic algorithms.

- parsing OOXML parts
- resolving relationships
- extracting ordered content
- mapping formulas to MathML
- mapping visual evidence to chart/graph representations

LLM layer handles **pragmatic interpretation**:

- business meaning
- intent
- policy reasoning
- narrative synthesis

Boundary keeps pipeline reliable:

- deterministic layer produces auditable evidence
- LLM layer reasons over evidence, not over raw OOXML internals

## Canonical Forms Captured

### Plain text and structure

- paragraph as `para`
- list as `list ordered="true|false"`
- table as `table -> row -> cell`
- links and references separated from plain prose

Examples of plain text represented in canonical form:

<kbd>![running text](src/main/resources/png/running-text.png)</kbd>


canonical output:
<table>
  <tr>
    <th>XML form</th>
    <th>S-Expression form</th>
  </tr>
  <tr>
    <td>
      <pre lang="xml">&lt;para label="h1" 
source-path="/word/document/p[1]"&gt;
  &lt;text&gt;Benchmark Document&lt;/text&gt;
&lt;/para&gt;
&lt;para source-path="/word/document/p[2]"&gt;
  &lt;text&gt;Paragraph with bold&lt;/text&gt;
&lt;/para&gt;
&lt;para source-path="/word/document/p[3]"&gt;
  &lt;text&gt;Visit https://example.com&lt;/text&gt;
&lt;/para&gt;
&lt;table&gt;
  &lt;row&gt;
    &lt;cell&gt;
      &lt;text&gt;Document version control&lt;/text&gt;
    &lt;/cell&gt;
  &lt;/row&gt;
  &lt;row&gt;
    &lt;cell&gt;
      &lt;text&gt;Version&lt;/text&gt;
    &lt;/cell&gt;
    &lt;cell&gt;
      &lt;text&gt;Issue Date&lt;/text&gt;
    &lt;/cell&gt;
    &lt;cell&gt;
      &lt;text&gt;Author&lt;/text&gt;
    &lt;/cell&gt;
    &lt;cell&gt;
      &lt;text&gt;Description of modification&lt;/text&gt;
    &lt;/cell&gt;
  &lt;/row&gt;
  &lt;row&gt;
    &lt;cell&gt;
      &lt;text&gt;1&lt;/text&gt;
    &lt;/cell&gt;
    &lt;cell&gt;
      &lt;text&gt;11 Aug 202 6&lt;/text&gt;
    &lt;/cell&gt;
    &lt;cell&gt;
      &lt;text&gt;J. S.Hildebrand&lt;/text&gt;
    &lt;/cell&gt;
    &lt;cell&gt;
      &lt;text&gt;Replacement of previous documents: 
A (2019-11) B (2019-07)&lt;/text&gt;
    &lt;/cell&gt;
  &lt;/row&gt;
&lt;/table&gt;
&lt;para label="h2" source-path="/word/document/p[5]"&gt;
  &lt;text&gt;Section A&lt;/text&gt;
&lt;/para&gt;
&lt;para source-path="/word/document/p[6]"&gt;
  &lt;text&gt;First item&lt;/text&gt;
&lt;/para&gt;
&lt;para source-path="/word/document/p[7]"&gt;
  &lt;text&gt;Second item&lt;/text&gt;
&lt;/para&gt;
&lt;para source-path="/word/document/p[8]"&gt;
  &lt;text&gt;Alpha&lt;/text&gt;
&lt;/para&gt;
&lt;list ordered="true"&gt;
  &lt;item&gt;
    &lt;text&gt;First item&lt;/text&gt;
  &lt;/item&gt;
  &lt;item&gt;
    &lt;text&gt;Second item&lt;/text&gt;
  &lt;/item&gt;
&lt;/list&gt;
&lt;para source-path="/word/document/p[9]"&gt;
  &lt;text&gt;Beta&lt;/text&gt;
&lt;/para&gt;
&lt;list ordered="false"&gt;
  &lt;item&gt;
    &lt;text&gt;Alpha&lt;/text&gt;
  &lt;/item&gt;
  &lt;item&gt;
    &lt;text&gt;Beta&lt;/text&gt;
  &lt;/item&gt;
&lt;/list&gt;
&lt;table&gt;
  &lt;row&gt;
    &lt;cell&gt;
      &lt;text&gt;App&lt;/text&gt;
    &lt;/cell&gt;
    &lt;cell&gt;
      &lt;text&gt;Team&lt;/text&gt;
    &lt;/cell&gt;
  &lt;/row&gt;
  &lt;row&gt;
    &lt;cell&gt;
      &lt;text&gt;CRM&lt;/text&gt;
    &lt;/cell&gt;
    &lt;cell&gt;
      &lt;text&gt;Sales&lt;/text&gt;
    &lt;/cell&gt;
  &lt;/row&gt;
&lt;/table&gt;
&lt;para source-path="/word/document/p[10]"&gt;
  &lt;text&gt;[A] -&amp;gt; [B]&lt;/text&gt;
&lt;/para&gt;
&lt;para label="h2" source-path="/word/document/p[11]"&gt;
  &lt;text&gt;Section B&lt;/text&gt;
&lt;/para&gt;
&lt;para source-path="/word/document/p[12]"&gt;
  &lt;text&gt;Final paragraph&lt;/text&gt;
&lt;/para&gt;</pre>
    </td>
    <td valign="top">
      <pre lang="lisp">(.
  (para {label "h1" source-path "/word/document/p[1]"} (text "Benchmark Document"))
  (para {source-path "/word/document/p[2]"} (text "Paragraph with bold"))
  (para {source-path "/word/document/p[3]"} (text "Visit https://example.com"))
  (table
    (row (cell (text "Document version control")))
    (row
      (cell (text "Version"))
      (cell (text "Issue Date"))
      (cell (text "Author"))
      (cell (text "Description of modification")))
    (row
      (cell (text "1"))
      (cell (text "11 Aug 202 6"))
      (cell (text "J. S.Hildebrand"))
      (cell (text "Replacement of previous documents: A (2019-11) B (2019-07)"))))
  (para {label "h2" source-path "/word/document/p[5]"} (text "Section A"))
  (para {source-path "/word/document/p[6]"} (text "First item"))
  (para {source-path "/word/document/p[7]"} (text "Second item"))
  (para {source-path "/word/document/p[8]"} (text "Alpha"))
  (list {ordered "true"}
    (item (text "First item"))
    (item (text "Second item")))
  (para {source-path "/word/document/p[9]"} (text "Beta"))
  (list {ordered "false"}
    (item (text "Alpha"))
    (item (text "Beta")))
  (table
    (row (cell (text "App")) (cell (text "Team")))
    (row (cell (text "CRM")) (cell (text "Sales"))))
  (para {source-path "/word/document/p[10]"} (text "[A] -> [B]"))
  (para {label "h2" source-path "/word/document/p[11]"} (text "Section B"))
  (para {source-path "/word/document/p[12]"} (text "Final paragraph")))</pre>
    </td>
  </tr>
</table>

### Formulas

- DOCX OMML fragments transformed to MathML
- Math nodes embedded in canonical paragraph/cell content
- text flattening noise avoided for formula fidelity

![formula](src/main/resources/png/formula.png)

canonical output:
<table>
  <tr>
    <th>XML form</th>
    <th>S-Expression form</th>
  </tr>
  <tr>
    <td>
      <pre lang="xml">&lt;para&gt;
  &lt;math xmlns="http://www.w3.org/1998/Math/MathML"&gt;
    &lt;mrow&gt;
      &lt;msubsup&gt;
        &lt;mi&gt;CoverAmt&lt;/mi&gt;
        &lt;mi&gt;Cov&lt;/mi&gt;
        &lt;mi&gt;Perc&lt;/mi&gt;
      &lt;/msubsup&gt;
      &lt;mi&gt;=&lt;/mi&gt;
      &lt;msubsup&gt;
        &lt;mi&gt;CoverPerc&lt;/mi&gt;
        &lt;mi&gt;Cov&lt;/mi&gt;
        &lt;mi&gt;CR&lt;/mi&gt;
      &lt;/msubsup&gt;
      &lt;mi&gt;*&lt;/mi&gt;
      &lt;msub&gt;
        &lt;mi&gt;ExpAmt&lt;/mi&gt;
        &lt;mi&gt;ExpEvent&lt;/mi&gt;
      &lt;/msub&gt;
      &lt;mi&gt;when&lt;/mi&gt;
      &lt;msub&gt;
        &lt;mi&gt;OSGID&lt;/mi&gt;
        &lt;mi&gt;Cov&lt;/mi&gt;
        &lt;mi&gt;j&lt;/mi&gt;
      &lt;/msub&gt;
      &lt;mi&gt;=&lt;/mi&gt;
      &lt;mi&gt;null and New Cover Alloc Ind&amp;lt;&lt;/mi&gt;
      &lt;msup&gt;
        &lt;mi&gt;&amp;gt;&lt;/mi&gt;
        &lt;mi&gt;'&lt;/mi&gt;
      &lt;/msup&gt;
      &lt;msup&gt;
        &lt;mi&gt;Y&lt;/mi&gt;
        &lt;mi&gt;'&lt;/mi&gt;
      &lt;/msup&gt;
    &lt;/mrow&gt;
  &lt;/math&gt;
&lt;/para&gt;</pre>
    </td>
    <td  valign="top">
      <pre lang="lisp">(.
  (para
    (math {xmlns "http://www.w3.org/1998/Math/MathML"}
      (mrow
        (msubsup (mi "CoverAmt") (mi "Cov") (mi "Perc"))
        (mi "=")
        (msubsup (mi "CoverPerc") (mi "Cov") (mi "CR"))
        (mi "*")
        (msub (mi "ExpAmt") (mi "ExpEvent"))
        (mi "when")
        (msub (mi "OSGID") (mi "Cov") (mi "j"))
        (mi "=")
        (mi "null and New Cover Alloc Ind<")
        (msup (mi ">") (mi "'"))
        (msup (mi "Y") (mi "'"))))))</pre>
    </td>
  </tr>
</table>
### Flow charts

- Graph topology captured as GraphML `graph` evidence (`node`, `edge`, `group`, annotations)

![flowchart](src/main/resources/png/flowchart.png)

canonical output:
<table>
  <tr>
    <th>XML form</th>
    <th>S-Expression form</th>
  </tr>
  <tr>
    <td  valign="top">
      <pre lang="xml">&lt;graph xmlns="http://graphml.graphdrawing.org/xmlns" 
href="media/image1.emf" source-path="/word/document/p[2]/drawing[1]"&gt;
    &lt;node confidence="0.72" geometry="ellipse" id="469179847-start" semantic="root"&gt;
        &lt;label&gt;Start&lt;/label&gt;
    &lt;/node&gt;
    &lt;node confidence="0.73" geometry="rectangle" id="469179847-a-calculate-uncovered" semantic="process"&gt;
        &lt;label&gt;Calculate Uncovered Amount (see section a)&lt;/label&gt;
    &lt;/node&gt;
    &lt;node confidence="0.73" geometry="rectangle" id="469179847-b-alloc-before-haircut" semantic="process"&gt;
        &lt;label&gt;Calculate Allocated Cover Amount without excess before haircut (see section b)&lt;/label&gt;
    &lt;/node&gt;
    &lt;node confidence="0.73" geometry="rectangle" id="469179847-c-alloc-after-haircut" semantic="process"&gt;
        &lt;label&gt;Calculate Allocated Cover Amount without excess after haircut (see section c)&lt;/label&gt;
    &lt;/node&gt;
    &lt;node confidence="0.67" geometry="ellipse" id="469179847-end-inferred" semantic="leaf"&gt;
        &lt;label&gt;End&lt;/label&gt;
    &lt;/node&gt;
    &lt;edge confidence="0.77" directed="true" semantic="flow" source="469179847-start" target="469179847-a-calculate-uncovered"/&gt;
    &lt;edge confidence="0.77" directed="true" semantic="flow" source="469179847-a-calculate-uncovered" target="469179847-b-alloc-before-haircut"/&gt;
    &lt;edge confidence="0.77" directed="true" semantic="flow" source="469179847-b-alloc-before-haircut" target="469179847-c-alloc-after-haircut"/&gt;
    &lt;edge confidence="0.77" directed="true" semantic="flow" source="469179847-c-alloc-after-haircut" target="469179847-end-inferred"/&gt;
    &lt;group id="469179847-group-1" semantic="process-group"&gt;
        &lt;label&gt;Allocate Cover to Outstanding Group&lt;/label&gt;
        &lt;member node="469179847-a-calculate-uncovered"/&gt;
        &lt;member node="469179847-b-alloc-before-haircut"/&gt;
        &lt;member node="469179847-c-alloc-after-haircut"/&gt;
    &lt;/group&gt;
&lt;/graph&gt;</pre>
    </td>
    <td valign="top;">
      <pre lang="lisp">(.
  (graph {xmlns "http://graphml.graphdrawing.org/xmlns" href "media/image1.emf" source-path "/word/document/p[2]/drawing[1]"}
    (node {confidence "0.72" geometry "ellipse" id "469179847-start" semantic "root"} (label "Start"))
    (node {confidence "0.73" geometry "rectangle" id "469179847-a-calculate-uncovered" semantic "process"}
      (label "Calculate Uncovered Amount (see section a)"))
    (node {confidence "0.73" geometry "rectangle" id "469179847-b-alloc-before-haircut" semantic "process"}
      (label "Calculate Allocated Cover Amount without excess before haircut (see section b)"))
    (node {confidence "0.73" geometry "rectangle" id "469179847-c-alloc-after-haircut" semantic "process"}
      (label "Calculate Allocated Cover Amount without excess after haircut (see section c)"))
    (node {confidence "0.67" geometry "ellipse" id "469179847-end-inferred" semantic "leaf"} (label "End"))
    (edge {confidence "0.77" directed "true" semantic "flow" source "469179847-start" target "469179847-a-calculate-uncovered"})
    (edge {confidence "0.77" directed "true" semantic "flow" source "469179847-a-calculate-uncovered" target "469179847-b-alloc-before-haircut"})
    (edge {confidence "0.77" directed "true" semantic "flow" source "469179847-b-alloc-before-haircut" target "469179847-c-alloc-after-haircut"})
    (edge {confidence "0.77" directed "true" semantic "flow" source "469179847-c-alloc-after-haircut" target "469179847-end-inferred"})
    (group {id "469179847-group-1" semantic "process-group"}
      (label "Allocate Cover to Outstanding Group")
      (member {node "469179847-a-calculate-uncovered"})
      (member {node "469179847-b-alloc-before-haircut"})
      (member {node "469179847-c-alloc-after-haircut"}))))</pre>
    </td>
  </tr>
</table>

### Diagrams and XY charts with precision


- chart evidence captured as canonical `chart` (`axis`, `series`, ordered values)
- XY points preserve coordinate order from source evidence
- raster flow uses OpenCV preprocessing + PaddleOCR ONNX/DJL runtime path

![chart1.png](src/test/resources/puml/chart1.png)

canonical output:
<table>
  <tr>
    <th>XML form</th>
    <th>S-Expression form</th>
  </tr>
  <tr>
    <td>
      <pre lang="xml">&lt;chart href="media/image1.emf" 
source-path="/word/document/p[1]/drawing[1]"&gt;
  &lt;legend&gt;right&lt;/legend&gt;
  &lt;axis role="x"&gt;
    &lt;label&gt;t&lt;/label&gt;
  &lt;/axis&gt;
  &lt;axis role="y"&gt;
    &lt;label&gt;f(t)&lt;/label&gt;
  &lt;/axis&gt;
  &lt;series&gt;
    &lt;name&gt;Trajectory&lt;/name&gt;
    &lt;value&gt;(-10,0)&lt;/value&gt;
    &lt;value&gt;(2,10)&lt;/value&gt;
    &lt;value&gt;(5,30)&lt;/value&gt;
    &lt;value&gt;(8,45)&lt;/value&gt;
    &lt;value&gt;(10,50)&lt;/value&gt;
  &lt;/series&gt;
  &lt;series&gt;
    &lt;name&gt;Checkpoints&lt;/name&gt;
    &lt;value&gt;(1,12)&lt;/value&gt;
    &lt;value&gt;(6,34)&lt;/value&gt;
    &lt;value&gt;(7,47)&lt;/value&gt;
  &lt;/series&gt;
&lt;/chart&gt;</pre>
    </td>
    <td valign="top">
      <pre lang="lisp">(.
  (chart {href "media/image1.emf" source-path "/word/document/p[1]/drawing[1]"}
    (legend "right")
    (axis {role "x"} (label "t"))
    (axis {role "y"} (label "f(t)"))
    (series
      (name "Trajectory")
      (value "(-10,0)")
      (value "(2,10)")
      (value "(5,30)")
      (value "(8,45)")
      (value "(10,50)"))
    (series
      (name "Checkpoints")
      (value "(1,12)")
      (value "(6,34)")
      (value "(7,47)"))))</pre>
    </td>
  </tr>
</table>

## RAG/Chunking vs Canonicalisation

### Quick comparison

| Merit | RAG / chunking-first | Canonicalisation-first |
| --- | --- | --- |
| Token burning | High when chunks include layout noise and repeated headers | Lower through typed, compact structural extraction |
| Reasoning quality | Variable; depends on chunk boundaries and retrieval luck | Higher consistency from typed context + provenance |
| Traceability | Often weak; chunk offsets can drift | Strong; `source-path` and media linkage are explicit |
| Determinism | Low-medium; embedding changes affect recall | High in extraction layer |
| Implementation effort | Faster MVP | Higher upfront modelling effort |
| Multi-format consistency | Usually uneven | Strong once schema contract stabilises |

### SWOT split by merit

#### 1) Token economics

**Strengths**
- compact typed output reduces prompt bloat

**Weaknesses**
- canonical model maintenance cost

**Opportunities**
- hybrid retrieval on canonical nodes + selective raw excerpts

**Threats**
- schema drift can reintroduce verbosity

#### 2) Reasoning quality

**Strengths**
- explicit structures improve compositional reasoning (tables, formulas, flows)

**Weaknesses**
- over-normalisation can hide subtle layout clues

**Opportunities**
- graph + formula aware reasoning chains

**Threats**
- OCR errors in raster inputs can mislead downstream reasoning

#### 3) Operations and governance

**Strengths**
- deterministic outputs simplify regression testing and audit

**Weaknesses**
- broader extractor surface area to support over time

**Opportunities**
- stable canonical contract for cross-team tooling

**Threats**
- new Office features may require rapid extractor updates

## Implementation

### Core stack

- `docx4j` for OOXML package/part handling
- canonical Java model with `JAXB` annotations
- `CanonicalXmlSerializer` for stable canonical serialisation
- `OoXmlCanonicalizer` for format-specific extraction + ordered body assembly

### Formula handling (MathML)

- `OmmlMathTransformer` applies bundled XSLT (`/xsl/omml2mathml.xsl`)
- OMML -> MathML in namespace `http://www.w3.org/1998/Math/MathML`
- canonical output keeps math embedded in paragraph/cell context

### Diagram and XY-graph handling

- vector/raster assets resolved from OOXML relationships
- chart evidence emitted as canonical `chart`
- topology evidence emitted as GraphML `graph`
- `PngAssetRecognizer` uses:
  - OpenCV preprocessing
  - PaddleOCR ONNX/DJL runtime path
  - deterministic fixture fallback for benchmark stability

### Future expansion path

- deeper VSDX extraction
- richer SVG semantics (groups, markers, connector intent)
- stronger EMF structural extraction
- more explicit chart metadata (units, axis roles, typed coordinates)

## What Plugin Produces

- canonical structures: `para`, `list`, `table`, `link`, `reference`
- chart evidence: `chart` (`axis`, `series`, ordered values)
- diagram evidence: GraphML `graph` (`node`, `edge`, `group`, annotations)
- provenance attributes like `source-path`
- media linkage via `href`

Specification and acceptance criteria:

[ooxml-canonical-benchmark-spec-v1.md](ooxml-canonical-benchmark-spec-v1.md)

## Benchmarks

### Benchmark v1

Compact baseline corpus for deterministic regression.

| Source document | Canonical result |
| --- | --- |
| [src/test/resources/ooxml/v1-benchmark.docx](src/test/resources/ooxml/v1-benchmark.docx) | [samples/ooxml-canonical-benchmark-v1/v1-benchmark.docx.sample.xml](samples/ooxml-canonical-benchmark-v1/v1-benchmark.docx.sample.xml) |
| [src/test/resources/ooxml/v1-benchmark.pptx](src/test/resources/ooxml/v1-benchmark.pptx) | [samples/ooxml-canonical-benchmark-v1/v1-benchmark.pptx.sample.xml](samples/ooxml-canonical-benchmark-v1/v1-benchmark.pptx.sample.xml) |
| [src/test/resources/ooxml/v1-benchmark.xlsx](src/test/resources/ooxml/v1-benchmark.xlsx) | [samples/ooxml-canonical-benchmark-v1/v1-benchmark.xlsx.sample.xml](samples/ooxml-canonical-benchmark-v1/v1-benchmark.xlsx.sample.xml) |

### Benchmark v2

Formula + diagram-focused corpus.

| Source document | Canonical result |
| --- | --- |
| [src/test/resources/ooxml/v2-formulas.docx](src/test/resources/ooxml/v2-formulas.docx) | [samples/ooxml-canonical-benchmark-v2/v2-formulas.xml](samples/ooxml-canonical-benchmark-v2/v2-formulas.xml) |
| [src/test/resources/ooxml/v2-diagrams.docx](src/test/resources/ooxml/v2-diagrams.docx) | [samples/ooxml-canonical-benchmark-v2/v2-diagrams.xml](samples/ooxml-canonical-benchmark-v2/v2-diagrams.xml) |

### Benchmark v3

Visual-recognition and chart-evidence corpus.

| Source document | Canonical result |
| --- | --- |
| [src/test/resources/ooxml/v3-png.docx](src/test/resources/ooxml/v3-png.docx) | [samples/ooxml-canonical-benchmark-v3/v3-png.xml](samples/ooxml-canonical-benchmark-v3/v3-png.xml) |
| [src/test/resources/ooxml/v3-emf-chart.docx](src/test/resources/ooxml/v3-emf-chart.docx) | [samples/ooxml-canonical-benchmark-v3/v3-emf-chart.xml](samples/ooxml-canonical-benchmark-v3/v3-emf-chart.xml) |
| [src/test/resources/ooxml/v3-png-chart.docx](src/test/resources/ooxml/v3-png-chart.docx) | [samples/ooxml-canonical-benchmark-v3/v3-png-chart.xml](samples/ooxml-canonical-benchmark-v3/v3-png-chart.xml) |

### Determinism checks

`OoXmlCanonicalizerTest` reruns benchmark fixtures and asserts byte-identical serialisation.

## Plugin ID

- `name.jurgenei.gradle.ooxml`

## Tasks

- `ooxmlToCanonical` (`name.jurgenei.gradle.ooxml.OoXmlToCanonicalTask`)
  - converts OOXML documents to canonical zip packages (`canonical.xml` + `media/*`)
- `extractAssets` (`name.jurgenei.gradle.ooxml.ExtractAssetsTask`)
  - extracts media and embedded assets from OOXML packages
- `validateCanonical` (`name.jurgenei.gradle.ooxml.ValidateCanonicalTask`)
  - validates canonical XML against `canonical.xsd`

## Gradle Usage

### Minimal setup

```groovy
plugins {
    id 'name.jurgenei.gradle.ooxml'
}
```

### Convert full directory tree (single root)

```groovy
tasks.named('ooxmlToCanonical', name.jurgenei.gradle.ooxml.OoXmlToCanonicalTask) {
    source(fileTree(layout.projectDirectory.dir('docs')) {
        include '**/*.docx', '**/*.pptx', '**/*.xlsx'
    })
    outputDirectory.set(layout.buildDirectory.dir('ooxml/canonical'))
}
```

### Convert multiple directory trees

```groovy
tasks.named('ooxmlToCanonical', name.jurgenei.gradle.ooxml.OoXmlToCanonicalTask) {
    source(fileTree(layout.projectDirectory.dir('docs/architecture')) {
        include '**/*.docx', '**/*.pptx', '**/*.xlsx'
    })
    source(fileTree(layout.projectDirectory.dir('docs/policies')) {
        include '**/*.docx', '**/*.pptx', '**/*.xlsx'
    })
    source(fileTree(layout.projectDirectory.dir('vendor-drop')) {
        include '**/*.docx', '**/*.pptx', '**/*.xlsx'
    })
    outputDirectory.set(layout.buildDirectory.dir('ooxml/canonical'))
}
```

### Emit legacy flat XML instead of zip packages

```groovy
tasks.named('ooxmlToCanonical', name.jurgenei.gradle.ooxml.OoXmlToCanonicalTask) {
    source(fileTree(layout.projectDirectory.dir('docs')) {
        include '**/*.docx', '**/*.pptx', '**/*.xlsx'
    })
    legacyXmlOutput.set(true)
    targetExtension.set('.xml1')
    outputDirectory.set(layout.buildDirectory.dir('ooxml/canonical-xml'))
}
```

### Emit canonical S-expression payload

```groovy
tasks.named('ooxmlToCanonical', name.jurgenei.gradle.ooxml.OoXmlToCanonicalTask) {
    source(fileTree(layout.projectDirectory.dir('docs')) {
        include '**/*.docx', '**/*.pptx', '**/*.xlsx'
    })
    targetExtension.set('.sexpr')
    outputDirectory.set(layout.buildDirectory.dir('ooxml/canonical'))
}
```

Canonicalisation target extension contract:

- `targetExtension.set('.xml1')` (default): serialize canonical payload as XML
- `targetExtension.set('.sexpr')`: serialize canonical payload as canonical S-expression

Canonical output (same content, two serializations):

<table>
  <tr>
    <th>XML form</th>
    <th>S-Expression form</th>
  </tr>
  <tr>
    <td>
      <pre lang="xml">&lt;document xmlns="http://jurgenei.name/canonical"&gt;
  &lt;metadata&gt;
    &lt;documentId&gt;sample&lt;/documentId&gt;
    &lt;version&gt;v1&lt;/version&gt;
  &lt;/metadata&gt;
  &lt;body&gt;
    &lt;para source-path="/word/document/p[1]"&gt;
      &lt;text&gt;Hello&lt;/text&gt;
    &lt;/para&gt;
  &lt;/body&gt;
&lt;/document&gt;</pre>
    </td>
    <td valign="top">
      <pre lang="lisp">(.
  (document {xmlns "http://jurgenei.name/canonical"}
    (metadata
      (documentId "sample")
      (version "v1"))
    (body
      (para {source-path "/word/document/p[1]"}
        (text "Hello")))))</pre>
    </td>
  </tr>
</table>

### Extract assets for review

```groovy
tasks.named('extractAssets', name.jurgenei.gradle.ooxml.ExtractAssetsTask) {
    source(fileTree(layout.projectDirectory.dir('docs')) {
        include '**/*.docx', '**/*.pptx', '**/*.xlsx'
    })
    outputDirectory.set(layout.buildDirectory.dir('ooxml/assets'))
}
```

### Validate canonical outputs

```groovy
tasks.named('validateCanonical', name.jurgenei.gradle.ooxml.ValidateCanonicalTask) {
    inputDirectory.set(layout.buildDirectory.dir('ooxml/canonical'))
}
```

### Representative run commands

```bash
./gradlew ooxmlToCanonical
./gradlew extractAssets
./gradlew validateCanonical
```

## Output conventions

- canonical namespace element names are lowercase
- paragraph element name is `para`
- body can contain structural + visual evidence in source order
- canonical package name uses input stem + extension (`document.docx` -> `document_docx.zip`)
- package contains:
  - `canonical.xml1` (default) or `canonical.sexpr`
  - `media/<asset-file>` referenced by `href`
- legacy flat mode writes `<stem><targetExtension>` (`v2-diagrams.xml1`, `v2-diagrams.sexpr`)

## Extension

- `ooxml.canonicalSchemaUrl`
  - URL to `canonical.xsd` for cross-plugin use

## Interop with `gradle-xml-plugin`

- pass `ooxml.canonicalSchemaUrl` into downstream XML/Schematron workflows
- sample bootstrap project: `samples/schematron-bootstrap-xml`

## Development

Run tests:

```bash
./gradlew test
```

Run canonicaliser tests only:

```bash
./gradlew test --tests name.jurgenei.gradle.ooxml.OoXmlCanonicalizerTest
```

Run task-level conversion tests:

```bash
./gradlew test --tests name.jurgenei.gradle.ooxml.OoXmlToCanonicalTaskTest
```

Regenerate benchmark samples:

```bash
./gradlew test --tests name.jurgenei.gradle.ooxml.GenerateSamples.regenerateSampleXmlFiles
```

Generate schema-first JAXB sources:

```bash
./gradlew generateCanonicalJaxb
```

## Test layout

- unit extraction tests: `src/test/java/name/jurgenei/gradle/ooxml/OoXmlCanonicalizerTest.java`
- task conversion tests: `src/test/java/name/jurgenei/gradle/ooxml/OoXmlToCanonicalTaskTest.java`
- functional TestKit tests: `src/test/java/name/jurgenei/gradle/ooxml/OoXmlPluginFunctionalTest.java`
- asset extraction tests: `src/test/java/name/jurgenei/gradle/ooxml/ExtractAssetsTaskTest.java`

