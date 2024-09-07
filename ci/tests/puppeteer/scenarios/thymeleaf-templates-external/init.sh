#!/bin/bash

echo "Creating static pages directory"
mkdir -p /tmp/cas/static/pages
echo "Hello World" > /tmp/cas/static/pages/index.txt
echo "Printing static page content"
cat /tmp/cas/static/pages/index.txt
