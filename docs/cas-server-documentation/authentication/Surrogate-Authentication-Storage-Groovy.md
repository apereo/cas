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
    def (surrogate,principal,logger) = args
    logger.info("Checking wildcard access {}", surrogate)
    return false
}

def canAuthenticate(Object... args) {
    def (surrogate,principal,service,logger) = args
    logger.info("Checking surrogate access for {}", surrogate)
    def accounts = getAccounts(principal.id, logger)
    return accounts.contains(surrogate)
}

def getAccounts(Object... args) {
    def (user,logger) = args
    logger.info("Getting authorized accounts for {}", user)
    return []
}
```

{% include_cached casproperties.html properties="cas.authn.surrogate.groovy" %}
