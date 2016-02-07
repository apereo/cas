#!/usr/bin/env bash

gradle clean build  -x test -x javadoc -x checkstyleMain -x checkstyleTest  -x signArchives  -x findbugsMain
