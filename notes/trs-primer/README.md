## Build instructions

Make sure you have Pandoc installed. Run

    pandoc --template=template.html -w html5 --section-divs -o trs-primer.html trs-primer.md

You can now open `trs-primer.html`. You don't have to edit raw HTML, just update the `trs-primer.md` Markdown source and re-run the command above.

### Regex to replace headings with manual numbering

Search: `(#+)\s*\d+(\.\d+(\.\d+(\.\d+)?)?)?\.?`
Replace: `$1`
