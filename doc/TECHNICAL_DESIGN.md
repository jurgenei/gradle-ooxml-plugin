# Technical Design - gradle-ooxml-plugin

## 1. Purpose

`gradle-ooxml-plugin` converts OOXML documents (`.docx`, `.pptx`, `.xlsx`) into canonical XML for downstream lineage, validation, and automation.

Core output goals:

- stable document metadata
- ordered structural content (paragraphs, lists, tables, links, references)
- graph representation for diagrams in GraphML namespace
- extracted media references with recognizer annotations

## 2. Scope

In scope:

- Gradle tasks for canonicalization, asset extraction, and schema validation
- OOXML package parsing and canonical model mapping
- pluggable asset recognizers
- EMF-focused diagram inference
- suspicious singleton graph recovery

Out of scope:

- full-fidelity rendering of drawing geometry
- OCR for non-text vector paths
- semantic correctness guarantees for every business diagram

## 3. High-Level Architecture

```text
OOXML input (.docx/.pptx/.xlsx)
        |
        v
+-----------------------+
| OoXmlToCanonicalTask  |
+-----------------------+
        |
        v
+-----------------------+
| OoXmlCanonicalizer    |
| - parse XML parts     |
| - preserve order      |
| - resolve rels/media  |
+-----------------------+
        |
        +-------------------------> Diagram extraction
                                      |
                                      v
                            +-----------------------+
                            | RecognizerRegistry    |
                            | - EmfAssetRecognizer  |
                            | - TextSnippetRecognizer (fallback)
                            +-----------------------+
                                      |
                                      v
                            GraphML graph + annotations
        |
        v
+-----------------------+
| CanonicalXmlSerializer|
+-----------------------+
        |
        v
canonical.xml (plain xml or packaged zip)
```

## 4. Gradle Plugin Components

### 4.1 Plugin Entry

- `OoXmlPlugin`
- registers extension `ooxml`
- registers tasks:
  - `ooxmlToCanonical`
  - `extractAssets`
  - `validateCanonical`

### 4.2 Extension

- `OoXmlExtension`
- properties:
  - `canonicalSchemaUrl`
  - `recognizerClasses` (custom recognizer class names)

### 4.3 Tasks

- `OoXmlToCanonicalTask`
  - validates input with `OpenXmlValidator`
  - canonicalizes each OOXML file
  - writes canonical output (`.xml` or packaged `.zip`)
- `ExtractAssetsTask`
  - extracts media payloads
- `ValidateCanonicalTask`
  - validates canonical output against `canonical.xsd` (imports `graphml.xsd`)

## 5. Canonical Data Model

Root namespace:

- `http://jurgenei.name/canonical`

Graph namespace:

- `http://graphml.graphdrawing.org/xmlns`

Body content:

- canonical elements: `Paragraph`, `List`, `Table`, `Link`, `Reference`
- graph elements: `graph` with child `node`, `edge`, `group`, `annotation`

Graph attributes:

- node: `id`, `geometry`, `semantic`, `confidence`, `label`
- edge: `source`, `target`, `semantic`, `confidence`, optional `label` (`Y/N`)
- group: `id`, `semantic`, `label`, `member`
- group member: `node` or nested `group`
- annotation: `kind`, `target`, `confidence`, `text`

## 6. Diagram Recognition Design

### 6.1 Recognizer SPI

- `AssetRecognizer`
  - `supports(extension, bytes)`
  - `recognize(assetId, assetPath, bytes)`
- `AssetRecognition`
  - nodes, edges, groups, annotations
- `RecognizerRegistry`
  - service-loaded recognizers + fallback recognizer

### 6.2 EMF Path

- `EmfAssetRecognizer`
- parses EMF records for coarse stats and text extraction
- delegates topology synthesis to fallback text recognizer when geometry extraction insufficient
- appends diagnostic annotation `kind="emf-stats"`

### 6.3 Text Fallback + Recovery

- `TextSnippetRecognizer`
- normal path:
  - tokenize `asset-text`
  - detect known diagram archetypes:
    - haircut formula flow
    - VRE decision flow
    - eligibility indicator flow
- suspicious singleton recovery path:
  - trigger when `nodes <= 1`, `edges == 0`, token richness high
  - synthesize process/decision nodes
  - synthesize sequential flow edges
  - infer optional `Y/N` edge labels
  - synthesize nested groups from hierarchy tokens
  - append `kind="recovery"` annotation

### 6.4 Confidence Strategy

- `ConfidenceModel`
- smoothed score using:
  - matched vs expected signals
  - sample size representativeness
- bounded range to avoid extreme certainty on weak evidence

## 7. Serialization Strategy

- `CanonicalXmlSerializer`
- marshals JAXB model
- normalizes GraphML output style to compact form:
  - canonical root stays default canonical namespace
  - each graph uses local default GraphML namespace (`<graph xmlns="...">`)

## 8. Schema Design

- `canonical.xsd`
  - canonical root and non-graph structures
  - imports `graphml.xsd`
  - references GraphML `graph` element in body
- `graphml.xsd`
  - graph/node/edge/group/annotation types used by plugin
  - supports nested groups via `member group="..."`

## 9. End-to-End Flow Details

1. Collect inputs (`InputCollector`)
2. Validate OOXML structure (`OpenXmlValidator`)
3. Parse package parts and relationships
4. Build ordered canonical body
5. For each drawing asset:
   - read binary payload
   - resolve recognizer via registry
   - infer graph topology
   - attach annotations and confidence
6. Serialize canonical XML
7. Optionally package with media and metadata
8. Validate against schema

## 10. Observability and Audit

### 10.1 In-band diagnostics

- `asset` annotation
- `asset-text` annotation
- `emf-stats` annotation
- `inferred-flow` annotation
- `recovery` annotation

### 10.2 Audit harness

- `DiagramAuditRunner` (test utility)
  - canonicalizes single document
  - counts suspicious singleton graphs
  - prints suspicious `source-path` and text preview
- `ExternalFdDocAuditTest`
  - optional environment-local audit against large FD document

## 11. Testing Strategy

Test layers:

- unit-like recognizer tests
  - `TextSnippetRecognizerTest`
  - `EmfAssetRecognizerTest`
- canonicalization behavior tests
  - `OoXmlCanonicalizerTest`
- Gradle task tests
  - `OoXmlToCanonicalTaskTest`, `ValidateCanonicalTaskTest`, `OoXmlPluginTest`
- sample regeneration test utility
  - `GenerateSamples`

Quality gates:

- topology presence checks (nodes/edges/groups)
- decision and branch-label checks
- nested-group checks
- deterministic serialization checks
- schema validation checks

## 12. Performance Considerations

- recognizers run per asset; avoid expensive global NLP
- token-based synthesis uses bounded candidate lists
- fallback heuristics operate in-memory only
- classloading via ServiceLoader done once per registry creation

## 13. Risks and Mitigations

Risk: over-generation on non-process diagrams (legend/ecosystem maps).

Mitigation:

- suspicious filters in audit
- constrained candidate limits
- recovery diagnostics for review

Risk: text-only EMF misses geometry semantics.

Mitigation:

- keep EMF record stats
- roadmap to geometry-aware extraction

Risk: template drift with new document styles.

Mitigation:

- pluggable recognizers
- add targeted archetypes incrementally

## 14. Roadmap

Near-term:

1. extract real shape/connector geometry from EMF records
2. bind text to nearest geometry
3. improve branch-direction and Y/N placement
4. add benchmark metrics (node/edge/group precision-recall)

Mid-term:

1. richer diagram archetype classifier
2. OCR fallback for vector text gaps
3. confidence component breakdown (`text/shape/topology`)

## 15. Usage Summary

Typical plugin usage:

```groovy
plugins {
    id 'name.jurgenei.gradle.ooxml'
}

ooxml {
    registerRecognizer('com.example.CustomRecognizer')
}
```

Task execution:

```zsh
./gradlew ooxmlToCanonical
./gradlew validateCanonical
```

---

Design principle: maximize unattended recovery quality with bounded heuristic cost, preserve deterministic canonical output, keep extension points open for format-specific recognizers.

