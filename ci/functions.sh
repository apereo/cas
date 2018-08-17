#!/bin/bash

currentChangeSetContains() {
    results=`git diff --name-only HEAD~1`
    contains=false

    # Turn on for case-insensitive matching
    # shopt -s nocasematch

    for i in "$results"
        do
            :
            if [[ ("$i" =~ $1) || "${TRAVIS_COMMIT_MESSAGE}" =~ "[force build]" ]]; then
                echo "Found a match against pattern $1. Commit message: ${TRAVIS_COMMIT_MESSAGE}"
                return 0
            fi
    done
    return 1
}

currentChangeSetAffectsTests() {
    currentChangeSetContains "\.(java|groovy|xml|properties|yml|json)"
    return `(expr "$?" + 0)`
}

currentChangeSetAffectsStyle() {
    currentChangeSetContains "\.(java|groovy|xml)"
    return `(expr "$?" + 0)`
}

currentChangeSetAffectsJavadocs() {
    currentChangeSetContains "\.(java|groovy)"
    return `(expr "$?" + 0)`
}

currentChangeSetAffectsDocumentation() {
    currentChangeSetContains "\.md"
    return `(expr "$?" + 0)`
}

currentChangeSetAffectsDependencies() {
    currentChangeSetContains "\.(gradle|properties)"
    return `(expr "$?" + 0)`
}

currentChangeSetAffectsSnapshots() {
    currentChangeSetContains "\.(java|groovy|yml|properties)"
    return `(expr "$?" + 0)`
}
