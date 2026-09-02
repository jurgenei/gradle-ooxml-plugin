# Singleton Graph Recovery

## Goal

Reduce suspicious diagrams where graph has only one `Start` node but `asset-text` carries many process labels.

## Implemented

- `TextSnippetRecognizer` now runs unattended recovery when:
  - `nodes <= 1`
  - `edges == 0`
  - `asset-text` token count >= 8
- Recovery synthesizes:
  - process/decision nodes from ranked token phrases
  - flow edges
  - optional branch labels (`Y` / `N`)
  - nested groups from short leading hierarchy labels
- Adds annotation:
  - `kind="recovery"`

## Quick Audit Runner

Use runner to canonicalize one document and count suspicious singleton graphs.

```zsh
cd /Users/cs79en/Developer/GitHub/gradle/gradle-ooxml-plugin
./gradlew --no-daemon testClasses
java -cp build/classes/java/main:build/classes/java/test name.jurgenei.gradle.ooxml.DiagramAuditRunner "/Users/cs79en/Developer/Projects/lineage/work/src/fd/FD Cover Allocation v86.0.docx"
```

Runner outputs:

- canonical output file path
- total graph count
- suspicious singleton count

