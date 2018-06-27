#!/bin/bash

currentChangeSetContains() {
    results=`git diff --name-only HEAD~1`
    contains=false

    # Turn on for case-insensitive matching
    # shopt -s nocasematch

    for i in "$results"
        do
            :
            if [[ "$i" =~ $1 ]]; then
                return 0
            fi
    done
    return 1
}

currentChangeSetAffectsTests() {
    currentChangeSetContains "java|groovy|xml|properties|yml|json"
    retval=$?
    return retval
}

currentChangeSetAffectsStyle() {
    currentChangeSetContains "java|groovy|xml"
    retval=$?
    return retval
}

currentChangeSetAffectsJavadocs() {
    currentChangeSetContains "java|groovy"
    retval=$?
    return retval
}

currentChangeSetAffectsDocumentation() {
    currentChangeSetContains "md"
    retval=$?
    return retval
}

currentChangeSetAffectsDependencies() {
    currentChangeSetContains "gradle|properties"
    retval=$?
    return retval
}

currentChangeSetAffectsSnapshots() {
    currentChangeSetContains "java|groovy|yml|properties"
    retval=$?
    return retval
}
