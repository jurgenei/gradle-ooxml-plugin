# OOXML Canonical Benchmark v2 Samples

This folder contains canonical XML samples for the benchmark corpus in `src/test/resources/ooxml/`.

## Files

- `v2-formulas.xml`

## Source fixture

- `src/test/resources/ooxml/v2-formulas.docx`

## Regeneration workflow

Use the plugin task against the fixture and compare the output with this sample:

```bash
./gradlew test --tests name.jurgenei.gradle.ooxml.OoXmlCanonicalizerTest.canonicalizesDocxFormulasAsMathMlAndPreservesOrder
```

