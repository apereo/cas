#!/bin/bash

echo "Creating static pages directory"
mkdir -p /etc/cas/static/pages
echo "Hello World" > /etc/cas/static/pages/index.txt
echo "Printing static page content"
cat /etc/cas/static/pages/index.txt
