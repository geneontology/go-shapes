# Python GO-CAM validator

## Installation

```
. init.sh
pip install -r requirements.txt
```

## Running Tests

```
make test
```

Currently this will

 - check syntax of shex file is correct
 - ensure all in should_pass actually pass
 - ensure all in should_fail actually fail

## Running on selected files

`python ./gocam_validator.py ../test_ttl/go_cams/should_pass/test2.ttl`

## TODO

Test suite. just run CLI for now.

## How it works

This does not rely on pre-injection of categories. Instead the pattern is:

```
<MolecularFunctionClass> @<OwlClass> AND {
  rdfs:subClassOf [ GoMolecularFunction: ] ;
}

<MolecularFunction> @<GoCamEntity> AND EXTRA a {
  a @<MolecularFunctionClass> {1};
  enabled_by:  ( @<Protein> OR @<Complex> ) {0,1};
```

The validator code will inject `?c rdfs:subClassOf ?a` triples using a
SPARQL `subClassOf*` query on the triplestore as a poor-man's
reasoning step. This is cached using cachier (we may want a different
solution for production but this is fine for now).

For every `?i rdf:type ?c` we find the ancestors, and then assert that
`?c` is an ancestor (reflexive) if that ancestor is in the set of
upper level classes mapped to a shape.

currently this is in the code header:

```
cmap = {
    "http://purl.obolibrary.org/obo/GO_0003674" : "MolecularFunction",
    "http://purl.obolibrary.org/obo/GO_0008150" : "BiologicalProcess",
    "http://purl.obolibrary.org/obo/GO_0005575" : "CellularComponent"
}
```

but should obviously be moved out somewhere more declarative.

