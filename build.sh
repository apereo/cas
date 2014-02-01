#!/bin/sh
#

groovy build/Anchor.groovy -d current
jekyll build --safe
