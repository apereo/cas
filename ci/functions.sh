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
