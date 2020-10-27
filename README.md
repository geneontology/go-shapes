[![Build Status](https://travis-ci.com/geneontology/go-shapes.svg?branch=master)](https://travis-ci.com/geneontology/go-shapes)

# GO_Shapes

Using RDF Shapes to define the schema of Gene Ontology Causal Activity Models


# Schema

See [shapes/go-cam-shapes.shex](shapes/go-cam-shapes.shex)

# Relationship of ShEx to OWL

We use OWL as a universal formalism for both ontology development (see https://github.com/geneontology/go-ontology/) and for representing GO-CAMs (the former uses *class axioms*, the latter uses *axioms on individuals*, see https://www.w3.org/TR/owl2-primer/ for more info on these terms). The use of ShEx is complementary. OWL encodes "biological truths" and formal encodings of concepts, whereas ShEx is used as a data model constraining the "shape" of GO-CAMs. There are many ways of interconnecting instances in a GO-CAM that are biologically consistent, but we want to constrain these to a common "shape" or structure as this is more predictable for software to deal with.

# For developers and core contributors
Please see [Developer Documentation](DeveloperDocs.md)
