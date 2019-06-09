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

    net.open_services.shapechecker.Main
        [-v|--vocab vocabFileGlob|vocabURI ...]
        [-s|--shape shapeFileGlob|shapeURI ...]
        [-q|--quiet suppressedIssue ...]
        [-x|--exclude excludeURIPattern ...]
        [-N|--nocrosscheck]
        [-V|--verbose]
        [-D]

where:

    -v vocabFileGlob|vocabURI ...

Each `-v` argument provides the path to a local file, supporting shell-style expansion,
or the URI for a single resource,
containing the Turtle source for a single RDF vocabulary.

    -s shapeFileGlob|shapeURI ...

Each `-s` argument provides the path to a local file, supporting shell-style expansion,
or the URI for a single resource,
containing the Turtle source for one or more OSLC Shape resources.

    -q suppressedIssue ...

Each `-q` argument provides the local name of a warning to be suppressed.
Warning names are defined in `net.open_services.shapechecker.ResultModel`.
For example, `-q BadTermStatus` suppresses warnings about uses of
`vs:term_status` with values other than `stable` or `archaic`.

    -x excludeURIPattern ...

Each `-x` argument specifies a URI **not** to be loaded when chasing down cross-references
from vocabularies or shapes being checked. Use this to exclude older versions of the
vocabularies or shapes being checked, or vocabularies or shapes not yet published to
their intended location. Use `@base` in the source to specify the intended location
of a not-yet-published vocabulary or shape.

    --verbose -D

These optional flags enable extra progress or debugging information.

The RDF model with the summary and warnings is described in the Javadoc for `net.open_services.shapechecker.ResultModel`.
Issues are classified into informational messages, warnings, and errors;
the command returns a non-zero exit status if any errors are found.
