---
layout: default
title: CAS - Groovy - Authentication Policy
category: Authentication
---
{% include variables.html %}

# Groovy - Authentication Policy

Decide whether an authentication attempt succeeds with a Groovy script that inspects the authentication result and can reject it with an exception.

{% include_cached casproperties.html properties="cas.authn.policy.groovy" %}

The script may be designed as:

```groovy
import java.util.*
import org.apereo.cas.authentication.exceptions.*
import javax.security.auth.login.*

def run(Object... args) {
    def (authentication, context, applicationContext, logger) = args

    if (conditionYouMayDesign() == true) {
        return new AccountDisabledException()
    }
    return null
}

def shouldResumeOnFailure(Object... args) {
    def (failure, logger) = args

    if (failure instanceof AccountNotFoundException) {
        return true
    }
    return false
}
```

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](../integration/Apache-Groovy-Scripting.html).
