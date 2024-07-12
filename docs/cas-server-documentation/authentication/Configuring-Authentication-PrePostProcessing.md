---
layout: default
title: CAS - Configuring Authentication Pre/Post Processing
category: Authentication
---
{% include variables.html %}

# Authentication Pre/Post Processing
            
Tap into the CAS authentication engine to invoke pre/post processors.

## Authentication Pre-Processing

{% include_cached casproperties.html properties="cas.authn.core.engine.groovy-pre-processor" %}

The script itself may be designed as:

```groovy
def run(Object[] args) {
    def (transaction,logger) = args
    true
}

def supports(Object[] args) {
    def (credential,logger) = args
    true
}
```

## Authentication Post-Processing

{% include_cached casproperties.html properties="cas.authn.core.engine.groovy-post-processor" %}

The script itself may be designed as:

```groovy
def run(Object[] args) {
    def (builder,transaction,logger) = args
    true
}

def supports(Object[] args) {
    def (credential,logger) = args
    true
}
```

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](../integration/Apache-Groovy-Scripting.html).
