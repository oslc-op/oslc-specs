Validate the consistency of a set of OSLC shapes and vocabularies
=================================================================

This tool validates a set of RDF vocabulary documents, checking that each vocabulary follows the W3C and OSLC
best practices for publishing such documents, using the right properties for the ontology and the various RDF terms.

It also validates a set of OSLC Resource Shapes, again checking that each follows OSLC best practices.

Finally, the tool cross-checks the vocabularies and shapes, ensuring that each RDF term used in the shapes and in the name space
of a vocabulary is actually defined in that vocabulary, and that each term defined in the given vocabularies is used somewhere in a shape.

In its current form, the tool is a command line program. The vocabularies and shapes to be validated are passed as command line arguments.
Output is a simple textual report, summarizing the number of issues found, and a line describing each issue.

In the longer term, the intent is that the tool should become a web-based one,
with a front end that prompts the user to enter the vocabulary and shape,
and a prettier presentation of the results.

Command line usage
------------------

    net.open_services.shapechecker.Main -q suppressedIssue ... -s shapeFile|shapeURI ... -v vocabFile|vocabURI ...

where:


    -q suppressedIssue ...

Each `-q` argument provides the local name of a warning to be suppressed.
Warning names are defined in `net.open_services.shapechecker.ResultModel`.
For example, `-q BadTermStatus` suppresses warnings about uses of
`vs:term_status` with values other than `stable` or `archaic`.

    -s shapeFile|shapeURI ...

Each `-s` argument provides the path to a local file, or the URI for a file,
containing the Turtle source for one or more OSLC Shape resources.

    -v vocabFile|vocabURI ...

Each `-v` argument provides the path to a local file, or the URI for a file,
containing the Turtle source for a single RDF vocabulary.

The RDF model with the summary and warnings is described in the Javadoc for `net.open_services.shapechecker.ResultModel`.
Note that not all 'issues' that are noted are necessarily errors - for example, a vocabulary may define some terms used
in resource discovery and that are not defined in any resource shape.
