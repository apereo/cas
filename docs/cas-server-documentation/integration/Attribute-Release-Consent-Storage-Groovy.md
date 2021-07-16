---
layout: default
title: CAS - Attribute Release Consent
category: Attributes
---

{% include variables.html %}

# Groovy - Attribute Consent Storage

Consent operations may be handled via a Groovy script whose path is taught to CAS via settings.

The script may be designed as:

```groovy
import java.util.*
import org.apereo.cas.consent.*

def read(Object[] args) {
    def currentConsentDecisions = args[0]
    def logger = args[1]
    currentConsentDecisions
}

def write(Object[] args) {
    def consentDecision = args[0]
    def logger = args[1]
    true
}

def delete(Object[] args) {
    def decisionId = args[0]
    def principalId = args[1]
    def logger = args[2]
    !principalId.contains("-")
}

def deletePrincipal(Object[] args) {
    def principalId = args[0]
    def logger = args[1]
    !principalId.contains("-")
}
```
 

## Configuration

{% include casproperties.html properties="cas.consent.groovy" %}

