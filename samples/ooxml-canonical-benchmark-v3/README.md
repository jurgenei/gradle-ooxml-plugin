# OOXML Canonical Benchmark v3 Samples

This folder contains canonical XML samples for the benchmark corpus in `src/test/resources/ooxml/`.

## Files

- `v3-png.xml`

## Source fixture

- `src/test/resources/ooxml/v3-png.docx`

## Regeneration workflow

Use canonicalizer output for the fixture and compare with this sample:

```bash
./gradlew test --tests name.jurgenei.gradle.ooxml.OoXmlCanonicalizerTest.canonicalizesDocxPngDiagramsAsGraphAnnotations
```

