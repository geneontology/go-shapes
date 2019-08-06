# Scala GO-CAM validator

This Makefile uses `shaclex` and `ammonite` to validate GO-CAMs. Appropriate versions of these are included in the repo in `bin` (depending on jars in `lib`).

## Running tests

```
make test
```

This will:

- Use `enrich-with-superclasses.sc` to query `rdf.geneontology.org` for all superclasses of OWL classes in each GO-CAM, and create a `*-enriched.ttl` copy of the GO-CAM containing all these superclasses.
- Validate each enriched GO-CAM against the `go-cam-shapes.shex` schema using the `go-cam-shapes.shapeMap` shape map.

## Pass/Fail
- Shape maps produced for files within the `should_pass` directory MUST NOT contain any `@!` entries.
- Shape maps produced for files within the `should_fail` directory MUST contain some `@!` entries.
