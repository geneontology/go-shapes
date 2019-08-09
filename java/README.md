# Java GO-CAM validator

Uses https://github.com/iovka/shex-java to validate GO-CAM RDF.
Built with Eclipse and Maven.

## Installation

```
cd java
mvn install
```

## Testing
```
mvn test
```
This (also happens automatically during above install) will:

- Enrich each GO-CAM with subclass relations linking instances to root classes 
- then validate the resulting RDF models against the `go-cam-shapes.shex` schema using the `go-cam-shapes.shapeMap` shape map to look up the appropriate focus nodes.  
- It will show errors if the ttl files in the should_pass directories do not validate or if the ttl files in the should_fail directories do validate.  

## Running one off
```
mvn install
cd bin
java -jar go_shapes_cli.jar -s ../../shapes/go-cam-shapes.shex -m ../../shapes/go-cam-shapes.shapeMap -f ../../test_ttl/go_cams/should_fail/Test005-enabled_by_biological_process.ttl -e
```
The above is an example.  Change the shex, shapemap, and ttl files as you see fit.  
