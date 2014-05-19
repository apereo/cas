CAS Protocol Documentation
==========================

This module contains CAS protocol documentation in MarkDown format.

To generate output-files in other formats, we recommend the usage of [Pandoc](http://johnmacfarlane.net/pandoc/).

Example conversion to HTML:

`pandoc -s -S --toc --self-contained --template ../pandoc/html_template.pandoc cas_protocol_3_0.md -t html5 -o cas_protocol_3_0.html`


Example conversion to PDF:

`pandoc -s -S --toc cas_protocol_3_0.md --latex-engine=xelatex -o cas_protocol_3_0.pdf`


Preview:

To preview the html pages on GH, use the url

`http://htmlpreview.github.com/?https://raw.github.com/Jasig/cas/master/cas-server-protocol/<version>/cas_protocol_<version>.html`

Example:

http://htmlpreview.github.io/?https://raw.github.com/Jasig/cas/master/cas-server-protocol/3.0/cas_protocol_3_0.html