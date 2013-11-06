CAS Protocol Documentation
==========================

This module contains CAS protocol documentation in MarkDown format.

To generate output-files in other formats, we recommend the usage of [Pandoc](http://johnmacfarlane.net/pandoc/).

Example conversion to HTML:

`pandoc -s -S --toc --self-contained cas_protocol_3_0.md -t html5 -o cas_protocol_3_0.html`


Example conversion to PDF:

`pandoc -s -S --toc cas_protocol_3_0.md --latex-engine=xelatex -o cas_protocol_3_0.pdf`