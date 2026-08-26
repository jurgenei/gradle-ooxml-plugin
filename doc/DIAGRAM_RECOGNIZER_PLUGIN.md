# Diagram Recognizer Plugins

## What Changed

- Diagram recognition now uses pluggable `AssetRecognizer` implementations.
- Built-in `EmfAssetRecognizer` handles EMF assets with custom record scanning.
- `TextSnippetRecognizer` remains fallback for unknown/unsupported formats.
- Confidence values derive from `ConfidenceModel` using smoothed evidence ratios.

## Configure Custom Recognizers

Add recognizer classes in Gradle extension:

```groovy
plugins {
    id 'name.jurgenei.gradle.ooxml'
}

ooxml {
    registerRecognizer('com.example.CustomWmfRecognizer')
}
```

Each recognizer must:

- implement `name.jurgenei.gradle.ooxml.recognizer.AssetRecognizer`
- provide public no-arg constructor

## SPI Registration

Register implementation via `ServiceLoader`:

`src/main/resources/META-INF/services/name.jurgenei.gradle.ooxml.recognizer.AssetRecognizer`

## Validation

- `EmfAssetRecognizerTest` verifies benchmark graph quality for `v2-diagrams.docx`.
- `OoXmlToCanonicalTaskTest` verifies extension-driven recognizer class loading.

