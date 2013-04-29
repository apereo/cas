CAS Protocol Documentation
==========================

This module contains CAS protocol documentation in MarkDown format.

To generate output-files in other formats, we recommend the usage of Pandoc.

Example conversion to HTML:

`pandoc -s --toc cas_protocol_3_0.md -t html -o ~/cas_protocol_3_0.html`


Example conversion to PDF:
`pandoc -s --toc cas_protocol_3_0.md --latex-engine=xelatex -o ~/cas_protocol_3_0.tex`
`xelatex ~/cas_protocol_3_0.tex`