Validate the consistency of a set of OSLC shapes and vocabularies
=================================================================

This tool validates a set of RDF vocabulary documents, checking that each vocabulary follows the W3C and OSLC
best practices for publishing such documents, using the right properties for the ontology and the various RDF terms.

It also validates a set of OSLC Resource Shapes, again checking that each follows OSLC best practices.

Finally, the tool cross-checks the vocabularies and shapes,
ensuring that each RDF term used in the shapes and in the name space of a vocabulary
is actually defined in that vocabulary,
and that each term defined in the given vocabularies is used somewhere in a shape.

In its current form, the tool is a command line program.
The vocabularies and shapes to be validated are passed as command line arguments.
Output is a simple textual report, summarizing the number of issues found, and a line describing each issue.

This is not the easiest of tools to use, and the messages it produces are often obscure.
The author was overdosing on Jena and RDF models when it was written, and things fell into the old adage
"if you have a hammer, everything looks like a nail".
The error/warning handling should really be rewritten to produce plainly intelligible messages.
It uses annotations, and has an annotations processor, scapt, as part of the system; again, that's probably overkill.

Command line usage
------------------

    net.open_services.shapechecker.Main
        [-v|--vocab vocabFileGlob|vocabURI ...]
        [-s|--shape shapeFileGlob|shapeURI ...]
        [-q|--quiet suppressedIssue ...]
        [-x|--exclude excludeURIPattern ...]
        [-t|--threshold level]
        [-C|--constraints]
        [-N|--nocrosscheck]
        [-V|--verbose]
        [-D|--debug]

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
Warning names are defined in `net.open_services.shapechecker.Terms`.
For example, `-q BadTermStatus` suppresses warnings about uses of
`vs:term_status` with values other than `stable` or `archaic`. Other warnings include
`undefinedTerm` and `unusedTerm`.

    -x excludeURIPattern ...

Each `-x` argument specifies a regular expression for a set of URIs
**not** to be loaded when chasing down cross-references
from vocabularies or shapes being checked. Use this to exclude older versions of the
vocabularies or shapes being checked, or vocabularies or shapes not yet published to
their intended location. Use `@base` in the source to specify the intended location
of a not-yet-published vocabulary or shape.

    -t level

Provides a facility to filter out less critical messages. `level` must be one of:

- `Info` shows everything, and is the default
- `Warning` shows only Warning and Error
- `Error` shows only Error messages
- `None` shows only the summary, with no messages for individual issues

    -C

Enforces validation of the `ResourceShapeConstraints` metadata in the shapes,
and additional publication metadata in vocabularies.

    -N

Suppress the normal cross-checks between vocabularies and shapes
(that terms used in the shapes are defined in the vocabularies,
and that terms defined in the vocabularies are used in the shapes).

    -V
    -D

These optional flags enable extra progress or debugging information.
Multiple occurrences of the -D option increase the debug output.

The RDF model with the summary and warnings is described in the Javadoc for `net.open_services.shapechecker.ResultModel`.
Issues are classified into informational messages, warnings, and errors;
the command returns a non-zero exit status if any errors are found.
