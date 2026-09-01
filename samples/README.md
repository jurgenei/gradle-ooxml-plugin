# Samples

Minimal runnable sample projects for the OOXML Gradle plugin.

## Available samples

- `schematron-bootstrap-xml` - uses `gradle-xml-plugin` to bootstrap Schematron from `ooxml.canonicalSchemaUrl` and validates canonical XML.
- `ooxml-canonical-benchmark-v1` - representative canonical outputs for the v1 benchmark fixtures.
- `ooxml-canonical-benchmark-v2` - generated canonical output for the formula-focused v2 DOCX fixture.
- `ooxml-canonical-benchmark-v3` - generated canonical output for the PNG-focused v3 DOCX fixture.

## Run sample

From `gradle-ooxml-plugin` root:

```bash
./gradlew -p samples/schematron-bootstrap-xml verifySample
```

