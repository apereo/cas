---
layout: default
title: CAS - Surrogate Authentication
category: Authentication
---
{% include variables.html %}

# Groovy Surrogate Authentication

Surrogate accounts may be defined in an external Groovy script whose path is 
specified via the CAS configuration. The body of the script may be defined as such:
       
```groovy
import org.apereo.cas.authentication.principal.*

def isWildcardAuthorized(Object... args) {
    def (surrogate,principal,service,logger) = args
    logger.info("Checking wildcard access {}", surrogate)
    return false
}

def canAuthenticate(Object... args) {
    def (surrogate,principal,service,logger) = args
    logger.info("Checking surrogate access for {} and service ${service?.id}", surrogate)
    def accounts = getAccounts(principal.id, service, logger)
    return accounts.contains(surrogate)
}

def getAccounts(Object... args) {
    def (user,service,logger) = args
    logger.info("Getting authorized accounts for {} and service ${service?.id}", user)
    return []
}
```

{% include_cached casproperties.html properties="cas.authn.surrogate.groovy" %}

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](../integration/Apache-Groovy-Scripting.html).
