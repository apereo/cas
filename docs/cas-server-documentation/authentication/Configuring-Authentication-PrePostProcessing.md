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
    def transaction = args[0]
    def logger = args[1]
    true
}

def supports(Object[] args) {
    def credential = args[0]
    def logger = args[1]
    true
}
```

                                   
## Authentication Post-Processing

{% include_cached casproperties.html properties="cas.authn.core.engine.groovy-post-processor" %}

The script itself may be designed as:

```groovy
def run(Object[] args) {
    def builder = args[0]
    def transaction = args[1]
    def logger = args[2]
    true
}

def supports(Object[] args) {
    def credential = args[0]
    def logger = args[1]
    true
}
```

