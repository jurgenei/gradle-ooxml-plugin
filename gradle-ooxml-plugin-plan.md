# gradle-ooxml-plugin

## Implementation Sync (2026-08-08)

This section keeps the plan aligned with the current implementation in `src/main` and `src/test`.

### Implemented now

- Gradle plugin skeleton based on `gradle-docx4j-plugin` blueprint.
- Tasks:
  - `ooxmlToCanonical`
  - `extractAssets`
  - `validateCanonical`
- Working conversion path for:
  - DOCX -> canonical XML
  - PPTX -> canonical XML
  - XLSX -> canonical XML
- Canonical schema added at `src/main/resources/schema/canonical.xsd`.
- JAXB-annotated canonical model implemented for:
  - `Document`
  - `Metadata`
  - `Body`
  - `Paragraph`
  - `List` / `Item`
  - `Table` / `Row` / `Cell`
  - `Link` / `Reference`
  - `Diagram` / `Shape` / `Connector`
- Provenance currently emitted on `Paragraph`:
  - `source-document`
  - `source-path`
- Version resolver currently supports:
  - `*_v3`
  - `*_1.2`
  - `*_FINAL`, `*_DRAFT`, `*_SNAPSHOT`, `*_RC*`
- Deterministic test fixtures committed for:
  - `sample_v3.docx`
  - `sample.pptx`
  - `sample.xlsx`
- Unit + functional tests verify conversion, assets, schema validation, and plugin wiring.
- End-to-end functional test verifies DOCX + PPTX + XLSX conversion followed by `validateCanonical`.
- Schema-first JAXB generation pipeline added (`generateCanonicalJaxb`) using `canonical.xsd` with generated sources compiled under a dedicated package.

### Phase status

- Phase 1 (canonical schema): in progress (metadata/paragraph/list/table/link/reference/diagram vocabulary implemented; deeper constraints pending).
- Phase 2 (JAXB model): in progress (manual JAXB model extended and schema-first code generation pipeline implemented; migration to generated model pending).
- Phase 3+ (deeper extraction semantics): not started.

### Next implementation targets

- Add provenance consistently across non-paragraph canonical elements.
- Incrementally migrate runtime serialization from hand-written model to generated JAXB model package.
- Add stricter schema rules for connector integrity and reference validation.

## Vision

Create a Gradle plugin that converts Office Open XML documents into a stable, format-independent Canonical XML representation.

Supported formats:

```text
DOCX
PPTX
XLSX
```

The plugin is responsible only for canonicalization.

The plugin is not responsible for:

```text
Knowledge extraction
Terminology mining
Neo4j generation
Ontology construction
RDF generation
DITA generation
LLM processing
```

These concerns belong to downstream tooling.

---

# Scope

## Input

Office Open XML packages:

```text
*.docx
*.pptx
*.xlsx
```

---

## Output

Canonical XML:

```xml
<c:Document/>
```

plus extracted assets:

```text
images
embedded objects
charts
visio documents
```

---

## Goal

Transform OOXML complexity into a consistent XML vocabulary.

```text
OOXML
    ↓
gradle-ooxml-plugin
    ↓
Canonical XML
```

Everything downstream should consume Canonic*l XML and never require direct kno*ledge of:

```text
WordprocessingML
PresentationML
SpreadsheetML
DrawingML
OPC Packaging
```

---

# Design Principles

## Principle 1

OpenXML handling bel*ngs here.

Supported responsibilities:

```text
Package parsing
Relat*onship resolution
Style resolution*Numbering reconstruction
Diagram extraction
Metadata extraction
Version extraction
Tracebility generation
```

---

## Principle 2

No business semantics.

T*e plugin may identify:

```text
Se*tion
Paragraph
Table
Shape
Connector
```

It must not identify:

```t*xt
Application
Capability
Requirem*nt
System
Interface
Process
```

Those are downstream decisions.

---*
## Principle 3

Preserve evidence

The plugin should preserve:

```*ext
Structure
Ordering
Labels
Rela*ionships
Coordinates
References
Me*adata
```

Avoid interpretation.

*--

## Principle 4

Canonical XML is the contract.

The XML schema is the primary architectural artifact*

Implementation classes are secondary.

---

# Architecture

```text
OOXML
    ↓
Package Extraction
    ↓
Format Extractor
        ├─- DOCX
        ├── PPTX
        └── XLSX
    ↓
Canonical Model
    ↓
Canonical XML
```

---

# Proposed Package Structure

```text
name.jurgenei.gradle.ooxml
│
├── task
│
*── extractor
│   ├── docx
│   ├── pptx
│   └── xlsx
│
├── canonical
├── diagram
├── media
├── asset
├── serializer
└── schema
```
---

# Canonical Namespace

```xm*
xmlns:c="http://jurgenei.name/canonical"
```

---

# Canonical Vocabulary

## Root

```xml
<c:Document>
```
---

## Metadata

```xml
<c:Metadata>
    <c:DocumentId/>
    <c:Version/>
    <c:SourceFile/>
    <c:DocumentType/>
    <c:Created/>
    <c:Modified/>
    <c:Author/>
</c:Metadata>
```

---

## C*ntent Container

```xml
<c:Body>
```

---

# Structural Elements

## Section

```xml
<c:Section>
```

Represents:

```text
Document section
Slide regions
Logical content gro*ps
```

---

## Title

```xml
<c:T*tle>
```

Represents:

```text
Document title
Section title
Slide tit*e
```

---

## Paragraph

```xml
<c:Paragraph>
```

Represents:

```t*xt
Block text
```

---

## Text

```xml
<c:Text>
```

Represents:

```text
Inline text
```

# Lists
## List

```xml
<c:List>
```

Attributes:

```text
ordered
unordered
```

---

## Item

```xml
<c:Item>
```

---

# Tables

## Table

```xml
<c:Table>
```

---

## Row

```x*l
<c:Row>
```

---

## Cell

```xm*
<c:Cell>
```

---

# References

# Link

```xml
<c:Link>
```

---

*# Bookmark

```xml
<c:Bookmark>
```

---
## Reference

```xml
<c:Ref*rence>
```

---

# Notes

## Footnote

```xml
<c:Footnote>
```

---
# Endnote

```xml
<c:Endnote>
```
## Comment

```xml
<c:Comment*
```

---

# Media Vocabulary

## Figure

```xml
<c:Figure>
```

---
*## Image

```xml
<c:Image>
```

At*ributes:

```text
id
asset
contenttype
```

---

## Chart

```xml
<c:Chart>
```

---

## EmbeddedObjects
```xml
<c:EmbeddedObject>
```

Ty*es:

```text
Visio
Excel
PDF
Unkno*n
```

---

# Diagram Vocabulary

Diagrams are first-class canonical *lements.

---

## Diagram

```xml
<c:Diagram>
```

Container for diag*am topology.

---

## Shape

```xm*
<c:Shape>
```

Attributes:

```te*t
id
x
y
width
height
```

No sema*tic interpretation.

---

## Labels
```xml
<c:Label>
```

Represents *ext associated with:

```text
Shape
Textbox
SmartArt node
Drawing
```

## Connector

```xml
<c:Connector>
```

Attributes:

```text
so*rce
target
```

Preserves graphica* relationships.

No semantic meani*g is assigned.

---

## Group

```xml
<c:Group>
```

Preserves groupe* shapes.

---

## Note

```xml
<c:note>
```

Preserves callouts and a*notations.

---

# Provenance

Every canonical element should support:

```text
source-document
source-version
source-id
source-path
```

-*-

Example

```xml
<c:Paragraph
  * source-document="FD-123.docx"
   *source-version="4.1"
    source-id*"p42"
    source-path="/1/2/3">
```

# Format-Specific Extraction

## DOCX

Extract:

```text
Paragraphs
Runs
Lists
Tables
Images
Comments
Footnotes
Endnotes
Hyperlinks
*extboxes
DrawingML
SmartArt
Charts and Embedded objects
```

---

## PPTX
Extract:

```text
Slide titles
Slide text
Tables
Shapes
Textboxes
Connectors
SmartArt
Charts
Speaker notes
Embedded objects
```

---

## XLSX

Extract:

```text
Workbook metadata
Sheets
Tables
Rows
Cells
Comm*nts
Drawings
Shapes
Charts
Embedded objects
```

---

# Relationship resolution

Resolve:

```text
Images
Hyperlinks
Embedded packages
Footnotes
Comments
Chart references
Diagram references
```

before canonical serialisation.

No unresolved relationship identifiers should remain.

---

# Asset Extraction

## Output

```text
assets/
```

---

## Extract

```text
Images
Charts
VisioDocuments
Embedded Office document*
PDFs
Other embedded packages
```
*---

## Asset Inventory

Generate:
```xml
<c:AssetInventory>
```

containing:

```text
Asset id
Filename
Content Type
Location
```

---

#*Diagram Extraction

## Goal

Preserve diagram knowledge without interpretation.

---

## Supported Sources

### DOCX

```text
DrawingML
SmartArt
Textboxes
Connectors
Embedded*Visio
```

---

### PPTX

```text
Shapes
Connectors
SmartArt
Drawings*Architecture Diagrams
```

---

## XLSX

```text
Drawing Layer
Charts
Shapes
```

---

## Preserve

### Topology

```xml
<c:Connector
    *ource="a"
    target="b"/>
```

### Labels

```xml
<c:Shape id="sap">

    <c:Label>
        SAP
   </c:Label>

</c:Shape>
```

---

## Coordinates

```xml
<c:Shape
    x="100"
    y="240"
    width="80*
    height="40"/>
```

---

### Hierarchies

```xml
<c:Group>
```

and

```xml
<c:Diagram>
```

must pr*serve nesting structures.

---

# Version Handling

## Goal

Separate*identity from version.

Example:

*``text
CustomerData_v3.docx

becomes:

```xml
<c:DocumentId>
    customerData
</c:DocumentId>

<c:Version>
    3
</c:Version>
```

---
*## Version Resolver

Provide pluggable filename resolvers.

Examples:
```text
*_v3.docx
*_1.2.docx
*_FINAL.docx
```

---

# Canonical Schema

## Deliverables

```text
canonical.xsd
```

```text
canonical.xml
```

---

## Requirements

Stable across:

```text
DOCX
PPTX
XLSX
Future extractors
```

---

# JAXB Strategy

## Recommendation

Schema-first.

Define:

```text
canonical.xsd
```

First.

Generate JAXB classes from schema.

Avoid hand-building large canonical object hierarchies.

---

# Gradle Tasks

## Main Task

```text
ooxmlToCanonical
```

Input:

```text
DOCX
PPTX
XLSX
```

Output:

```text
canonical/*.xml
```

---

## Asset Task

```text
extractAssets
```

Output:

```text
assets/
```

---

## Validation Task

```text
validateCanonical
```

Uses:

```text
canonical.xsd
```

---

# MVP

The MVP is complete when:

- DOCX converts to Canonical XML.
- PPTX converts to Canonical XML.
- XLSX converts to Canonical XML.
- Metadata is preserved.
- Lists are reconstructed.
- Tables are reconstructed.
- References are resolved.
- Assets are extracted.
- Diagram topology is preserved.
- Provenance is available.
- Version information is preserved.
- Canonical XML validates against the schema.
- No OOXML-specific structures escape the plugin.
