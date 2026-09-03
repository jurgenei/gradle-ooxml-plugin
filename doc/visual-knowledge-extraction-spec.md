# Visual Knowledge Extraction and Canonicalization Specification

## Objective

Extend the Office document canonicalization pipeline to extract semantic knowledge from embedded visual artifacts.

The goal is not to preserve rendering details, but to preserve meaning in a deterministic, machine-readable form that can later be consumed by Copilot and other reasoning systems.

---

## Core Principle

Canonicalization should normalize by meaning, not by source format.

The same conceptual artifact may originate from:

- SmartArt
- DrawingML
- Visio XML
- VML
- EMF
- WMF
- SVG
- PNG
- JPEG

Equivalent artifacts should converge to the same canonical representation.

---

## Artifact Classification

The first question must not be:

"What file format is this?"

The first question must be:

"What kind of information object is this?"

Classify visual artifacts into categories such as:

- Diagram
- Chart
- Table
- Screenshot
- Illustration
- Mixed

Classification determines which semantic extractor is used.

---

## Source Priority

When multiple representations exist, prefer the richest semantic source.

Priority order:

1. SmartArt XML
2. Visio XML (VSDX)
3. DrawingML
4. VML
5. EMF
6. WMF
7. Raster Images (PNG/JPEG)

Structured representations should always be preferred over computer vision.

---

## Canonicalization Boundary

Canonicalization extracts observable facts.

It must not generate narrative interpretations.

Examples:

Allowed:

- Nodes
- Connectors
- Labels
- Hierarchies
- Axes
- Legends
- Data series
- Tables

Not part of canonicalization:

- Business conclusions
- Summaries
- Recommendations
- Explanations of intent

Those belong to downstream reasoning agents.

---

## Diagram Extraction

Extract:

- Shapes
- Labels
- Connectors
- Groups
- Hierarchies

Build a canonical graph representation:

- Nodes
- Edges
- Annotations

Preserve:

- Connectivity
- Directionality
- Labels
- Semantic candidates

Discard where possible:

- Styling
- Colors
- Fonts
- Precise rendering information

---

## Graph Analysis

Use JGraphT as an internal analysis layer.

JGraphT must not appear in the canonical XML.

Use topology to derive information such as:

- Root nodes
- Leaf nodes
- Connected components
- Cycles
- Branching structures
- Hierarchies

Topology is often a stronger semantic signal than geometry.

---

## Chart Extraction

Charts are not diagrams.

Extract:

- Title
- Axes
- Units
- Legends
- Series
- Data when recoverable

Produce a canonical chart representation.

Do not force charts into the node/edge model.

---

## Raster Images

For PNG/JPEG sources:

Pipeline:

1. Artifact Classification
2. OCR
3. Shape Detection
4. Structure Reconstruction
5. Semantic Extraction

Recommended technologies:

- PaddleOCR for text recognition
- OpenCV for visual detection

However, raster processing is always a fallback after structured Office representations have been exhausted.

---

## Vector Formats

EMF, WMF and SVG should not be handled as final semantic formats.

They are geometry sources.

Extract:

- Shapes
- Text
- Connectors
- Arrowheads

Then feed the resulting structures into the same semantic extraction pipeline used by other diagram sources.

---

## Mathematical Formulae

Already implemented.

OMML should be converted to MathML.

MathML is considered a semantic endpoint and should be preserved unchanged within its own namespace.

No additional canonical math schema should be introduced.

---

## Deterministic Extraction Principle

The extraction layer should remain deterministic.

Its responsibilities are:

- Structure discovery
- Relationship extraction
- Semantic normalization

The extraction layer should not generate free-text descriptions of images.

Narrative interpretation belongs to downstream AI reasoning.

Canonical XML should contain evidence.

Copilot should generate interpretations from that evidence.

---

## Success Criteria

The resulting canonical representation should allow downstream agents to reason about:

- Process flows
- Organizational structures
- Dependencies
- Business entities
- Technical architectures
- Charts and metrics
- Mathematical content

without requiring knowledge of Office-specific formats or rendering technologies.
