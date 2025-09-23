#!/bin/bash

echo "Creating static pages directory"
sudo mkdir -p /etc/cas/static/pages
echo "Hello World" | sudo tee /etc/cas/static/pages/index.txt
echo "Printing static page content"
sudo cat /etc/cas/static/pages/index.txt
