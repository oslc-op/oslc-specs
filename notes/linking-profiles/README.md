## Build instructions

OASIS Project Specifications, Standards and Project Notes require a specific publishing format and boilerplate content. These rules have been codified and supported in ReSpec, a fork of W3C ReSpec created to support the generation of OASIS documents. 

Editing HTML source can be tedious and error prone. To simplify document editing and maintenance, and to enable easier contribution by other OSLC-OP members, we are utilizing standard Markdown for editing OSLC-OP documents, and pandoc to translate that markdown into ReSpec source documents. The remaining document publishing processes are unchanged.

pandoc parameters in the template.html file include:

* $title$ - the title of the generated HTML document
* $abstract$ - the document abstract
* $body$ - the docuemnt body


These parameters are set in the .md file at the top of the file, delimited by --- characters as in:
```
---
title: Document Title
abstract: Document abstract (can be multi-line)
---
```
The rest of the Markdown source is included in $body$ when translated by pandoc. See https://pandoc.org for additional information.

Building this .md file converts the Markdown content into HTML based on template.html. The resulting document can be tranformed by ReSpec into an HTML document that follows OASIS document publishing guidelines. 

Make sure you have Pandoc installed. Run

    pandoc --template=template.html -w html5 --section-divs -o link-profiles.html link-profiles.md

You can now open `link-profiles.html`. You don't have to edit raw HTML, just update the `link-profiles.md` Markdown source and re-run the command above.

Edits may be required in the template.html document itself. Typicaly these edits include:

* respecConfig parameters (e.g., shortName, specStatus, revision, etc.)
* relatedWork references
* localBiblio references
* citationLabel
* editors
* appendices, including Acknowledgements

### Regex to replace headings with manual numbering

Search: `(#+)\s*\d+(\.\d+(\.\d+(\.\d+)?)?)?\.?`
Replace: `$1`
