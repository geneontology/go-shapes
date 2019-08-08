# Java GO-CAM validator

Uses https://github.com/iovka/shex-java to validate GO-CAM RDF.
Built with Eclipse and Maven.

## Running tests

Right now, the tests are aimed at the pre-reasoned files in the scala target directory.  So you need to go into that directory first, run >make test and wait for it to populate the scala/target/ directories with enriched*.ttl files.  Then

```
mvn test
```

This will:

- Validate each enriched GO-CAM against the `go-cam-shapes.shex` schema using the `go-cam-shapes.shapeMap` shape map to look up the appropriate focus nodes.  It will show errors if the ttl files in the should_pass directories do not validate or if the ttl files in the should_fail directories do validate.  

## Running one off
```
mvn install
cd bin
java -jar go_shapes_cli.jar -s ../../shapes/go-cam-shapes.shex -m ../../shapes/go-cam-shapes.shapeMap -f ../../scala/target/should_fail/example_missing_evidence-enriched.ttl
```
The above is an example.  Change the shex, shapemap, and ttl files as you see fit.  
