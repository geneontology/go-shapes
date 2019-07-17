[![Build Status](https://travis-ci.org/geneontology/go-shapes.svg?branch=master)](https://travis-ci.org/geneontology/go-shapes)

# GO_Shapes

Using RDF Shapes to define the schema of Gene Ontology Causal Activity Models

The schema here is intended to be synced with the corresponding [google doc](https://docs.google.com/document/d/1OsE19zh8KE_2wT3-8oJysqEGqy-fahnhcjthO34IWP4/edit#), but there may be cases of things being out of sync for now.

# Schema

See [shapes/go-cam-shapes.shex](shapes/go-cam-shapes.shex)

# Travis checks

Any changes to the schema should be made via a Pull Request. This will trigger travis, which will check the schema against known passes and failures

Currently error reporting may be opaque - we are working on this!

# How to author tests

See also [#38](https://github.com/geneontology/go-shapes/issues/38)

We have two sets of tests.

The first set are ttl files that are authored in Noctua and then copied across to this repo. They consist of 

 * [test_ttl/](test_ttl)
     * [go_cams/](test_ttl/go_cams)
          * [should_pass/](test_ttl/go_cams/should_pass)
          * [should_fail/](test_ttl/go_cams/should_fail)

The advantage of these is that the test files can be authored by
curators in Noctua. The disadvantage is that there is a lot of
extraneous stuff these so these do not conform to the concept of a
Minimal Working Example (MWE) in software testing. See [this wikipedia
page](https://en.wikipedia.org/wiki/Minimal_working_example) for
concepts.

We additionally have a second set of tests that can be authored by software developers and curators familiar with ttl. These are currently in:

 * [python/tests/data/](python/tests/data/)

(they move out of here later as they are not python specific)

These files are in two sets. Those starting `p-` are ttl files
expected to pass. Those starting `f-` and files expecting to fail.

The files that should be authored are those with suffix
`ttlite`. These should be edited in a text editor or IDE. These are compiled to ttl. See the [Makefile](python/Makefile) for details.

Note that the `p-` source files have embedded within them examples of where they deviate.

E.g. in [p-1.ttlite](python/tests/data/p-1.ttlite) there is a chunk:

```
#FAIL multiple-occurs-in
# ## adding a second enabled_by for ka1
# ka1: occurs_in: n2:
# .
#END
```

This is used to generate
[f-1-multiple-occurs-in.ttlite](python/tests/data/f-1-multiple-occurs-in.ttlite),
which has a violating cardinality constraint injected in

The idea is to author MWE files that illustrate the core concepts.

# Running in Python

You can run individual files through the python command line tool

See the [python](python) folder

# Running in Java (temporarily deprecated)

After cloning the repo (https://github.com/geneontology/GO_Shapes.git) and going to its root

```
>cd java
>maven install
>cd bin
#Test specific shapes for MF, BP, and CC nodes found in the input ttl via hard coded sparql query
>java -jar go_shapes_cli.jar -f ../../test_ttl/go_cams/should_pass/typed_reactome-homosapiens-Acetylation.ttl -s ../../shapes/go-cam-shapes.shex
#Test all non-bnodes in input ttl against all shapes in the go schema (https://github.com/geneontology/GO_Shapes/blob/master/shapes/go-cam-shapes.shex) 
>java -jar go_shapes_cli.jar -f ../../test_ttl/go_cams/should_pass/typed_reactome-homosapiens-Acetylation.ttl -s ../../shapes/go-cam-shapes.shex -all

```

