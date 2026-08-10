# OOXML Canonical Benchmark v1 Samples

This folder contains representative canonical XML samples for the benchmark corpus in `src/test/resources/ooxml/`.

## Files

- `v1-benchmark.docx.sample.xml`
- `v1-benchmark.pptx.sample.xml`
- `v1-benchmark.xlsx.sample.xml`

## Notes

- Samples are intentionally concise and highlight the structures used by benchmark assertions.
- Exact canonical output can include additional `Paragraph`, `Shape`, or provenance entries.
- Determinism and minimum structure coverage are enforced by unit tests in:
  - `src/test/java/name/jurgenei/gradle/ooxml/OoXmlCanonicalizerTest.java`
  - `src/test/java/name/jurgenei/gradle/ooxml/OoXmlToCanonicalTaskTest.java`

## Regeneration workflow

Run the plugin conversion task against the benchmark corpus to compare real output with these samples:

```bash
./gradlew test --tests name.jurgenei.gradle.ooxml.OoXmlCanonicalizerTest
```

