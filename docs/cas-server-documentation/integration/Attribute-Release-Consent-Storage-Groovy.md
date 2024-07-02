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
    def (currentConsentDecisions,logger) = args
    currentConsentDecisions
}

def write(Object[] args) {
    def (consentDecision,logger) = args
    true
}

def delete(Object[] args) {
    def (decisionId,principalId,logger) = args
    !principalId.contains("-")
}

def deletePrincipal(Object[] args) {
    def (principalId,logger) = args
    !principalId.contains("-")
}
```

## Configuration

{% include_cached casproperties.html properties="cas.consent.groovy" %}

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](../integration/Apache-Groovy-Scripting.html).

