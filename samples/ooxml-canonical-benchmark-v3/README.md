# OOXML Canonical Benchmark v3 Samples

This folder contains canonical XML samples for the benchmark corpus in `src/test/resources/ooxml/`.

## Files

- `v3-png.xml`
- `v3-emf-chart.xml`
- `v3-png-chart.xml`

## Source fixture

- `src/test/resources/ooxml/v3-png.docx`
- `src/test/resources/ooxml/v3-emf-chart.docx`
- `src/test/resources/ooxml/v3-png-chart.docx`

## Regeneration workflow

Use canonicalizer output for fixture and compare with sample.
Chart fixtures (`v3-emf-chart.docx`, `v3-png-chart.docx`) validate canonical `chart` output with ordered series points/values from `@startchart` evidence.
During PaddleOCR migration, benchmark fallback keeps chart extraction deterministic for v3 fixtures.

```bash
./gradlew test --tests name.jurgenei.gradle.ooxml.OoXmlCanonicalizerTest.canonicalizesDocxPngDiagramFixtureWithOcrAnnotations
./gradlew test --tests name.jurgenei.gradle.ooxml.OoXmlCanonicalizerTest.canonicalizesDocxEmfChartFixtureWithGraphEvidence
./gradlew test --tests name.jurgenei.gradle.ooxml.OoXmlCanonicalizerTest.canonicalizesDocxPngChartFixtureWithGraphEvidence
```

