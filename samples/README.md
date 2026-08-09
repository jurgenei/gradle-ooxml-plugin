# Samples

Minimal runnable sample projects for the OOXML Gradle plugin.

## Available samples

- `schematron-bootstrap-xml` - uses `gradle-xml-plugin` to bootstrap Schematron from `ooxml.canonicalSchemaUrl` and validates canonical XML.

## Run sample

From `gradle-ooxml-plugin` root:

```bash
./gradlew -p samples/schematron-bootstrap-xml verifySample
```

