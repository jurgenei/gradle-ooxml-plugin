# Canonicalization Executive Overview

## Purpose

`gradle-ooxml-plugin` converts `.docx`, `.pptx`, and `.xlsx` into one canonical contract so downstream services can process all document types consistently.

Canonicalization in this project means:

- normalize cross-format content into one structure (`canonical.xml`)
- preserve provenance (`source-path`) and diagram media linkage (`Diagram@href`)
- keep output deterministic for validation, transformation, and diff workflows

## Why the Current Architecture Uses docx4j First

The project requirement is strict and stable canonical output, not generic text extraction.

- `docx4j` aligns well with OOXML package and XML-first extraction.
- the internal canonical model keeps mappings explicit and regression-testable.
- output packaging (`*_ext.zip` with `canonical.xml` and referenced `media/*`) remains deterministic.

## Why Apache POI / Apache Tika Are Not the Primary Core Today

- **Apache POI**: excellent for Office object-model depth (especially XLSX), but replacing the canonical core now increases migration complexity and determinism risk.
- **Apache Tika**: excellent for broad detection/extraction, but too generic as a source of truth for schema-precise canonical structure.

## Forward Technology Path

These technologies are candidates for targeted enrichment behind feature flags:

- **VSDX -> Visio XML**
  - add direct Visio topology extraction into canonical `Diagram` nodes/edges
- **SVG -> DOM/Batik**
  - add robust vector parsing for shape/connector/text mapping
- **EMF -> FreeHEP**
  - improve extraction of vector primitives and embedded text from EMF/WMF
- **PNG -> OpenCV + Tesseract**
  - add OCR-based fallback for raster-only diagram artifacts

## Adoption Rules

1. keep existing canonical pipeline as default baseline
2. add one analyzer at a time with deterministic fixture tests
3. annotate inference confidence where heuristics are used
4. promote to default only after quality and stability thresholds are met

