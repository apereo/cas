#!/usr/bin/env bash

./gradlew clean install -DskipAspectJ=true -x javadoc -x test
