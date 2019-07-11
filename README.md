[![Build Status](https://travis-ci.org/geneontology/GO_Shapes.svg?branch=master)](https://travis-ci.org/geneontology/GO_Shapes)


# GO_Shapes
Using RDF Shapes to define the schema of Gene Ontology Causal Activity Models

# Running in Java
After cloning the repo (https://github.com/geneontology/GO_Shapes.git) and going to its root
>cd java
>maven install
>cd bin
#Test specific shapes for MF, BP, and CC nodes found in the input ttl via hard coded sparql query
>java -jar go_shapes_cli.jar -f ../../test_ttl/go_cams/should_pass/typed_reactome-homosapiens-Acetylation.ttl -s ../../shapes/go-cam-shapes.shex
#Test all non-bnodes in input ttl against all shapes in the go schema (https://github.com/geneontology/GO_Shapes/blob/master/shapes/go-cam-shapes.shex) 
>java -jar go_shapes_cli.jar -f ../../test_ttl/go_cams/should_pass/typed_reactome-homosapiens-Acetylation.ttl -s ../../shapes/go-cam-shapes.shex -all



