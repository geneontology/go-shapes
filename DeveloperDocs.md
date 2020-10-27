# Travis checks

Any changes to the schema should be made via a Pull Request. This will trigger travis, which will check the schema against known passes and failures

Currently error reporting may be opaque - we are working on this!

# How to author tests

See also [#38](https://github.com/geneontology/go-shapes/issues/38)

We have two sets of tests.

## ttl files authored in Noctua and then copied across to this repo. They consist of:

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

How to create a Noctua test file:
* Create a model to test in Noctua.
* Add a comment from the Model > Edit annotations menu:
** If the model is supposed to pass, the comment should start with PASS and a short explanation of why it should pass.
** If the model is supposed to fail, the comment should start with FAIL and a short explanation of why it should fail.
* Save the model.
* Go to Model > Export to OWL.
* Copy the OWL output.

Commit to the GitHub go-shapes/test_ttl/go_cams directory
* Go to the go-shapes repo, navigate to the https://github.com/geneontology/go-shapes/tree/master/test_ttl/go_cams directory
* If the model is supposed to PASS, open the 'should_pass' directory.
* Click on the button on the upper right corner 'Create new file'
* Paste the OWL in that new file.
* Name the file Test-###-short-explanation-for-fail-or-pass (don't include spaces in the filename).

* If the model is supposed to FAIL, do the same steps as above, creating the file in the 'should_fail' directory.

## ttl files authored by software developers and curators familiar with ttl

These are currently in:
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

# Running in Java
See [java/README.md](java/README.md)

# Running in Scala
See [scala/README.md](scala/README.md)
