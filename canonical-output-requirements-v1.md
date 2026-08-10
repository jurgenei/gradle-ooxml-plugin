# Canonical Output Requirements for Benchmark Corpus v1

## Purpose

The benchmark corpus is intended to verify that OOXML extraction preserves logical structure and not merely text content.

The canonical output is expected to:

1. Preserve document hierarchy.
2. Preserve containment relationships.
3. Preserve table structure.
4. Preserve graph/diagram topology.
5. Preserve provenance information where available.
6. Be deterministic across repeated runs.
7. Avoid layout-oriented OOXML details unless required for semantic interpretation.

## Acceptance Criteria

- DOCX produces sections, lists, table, hyperlink and graph structures.
- PPTX produces slides, titles, table and graph structures.
- XLSX produces worksheets, tables, named ranges and merged-cell information.
- No benchmark structure is silently flattened into plain paragraph text.
- Canonical output remains deterministic between executions.
