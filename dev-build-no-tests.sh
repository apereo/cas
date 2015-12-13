#!/usr/bin/env bash

./gradlew clean build -DskipAspectJ=true -x javadoc -x test
