#!/bin/bash

sudo ./gradlew clean checkstyleMain bootRepackage install --parallel -x test --stacktrace --build-cache
