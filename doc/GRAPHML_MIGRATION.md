# GraphML Migration

## Scope

Diagram serialization moved to GraphML namespace:

- canonical namespace: `http://jurgenei.name/canonical`
- graph namespace: `http://graphml.graphdrawing.org/xmlns`

## Output Shape

`Body` now emits `<g:graph>` elements instead of `<Diagram>`.

Inside each graph:

- `<g:node>`
- `<g:edge>`
- `<g:group>`
- `<g:annotation>`

## Schema

- `canonical.xsd` imports `graphml.xsd`
- `BodyType` references `g:graph`

## Validation

`ValidateCanonicalTask` resolves schema by URL so imported `graphml.xsd` is loaded correctly.

