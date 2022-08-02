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
    def surrogate = args[0].toString()
    def principal = args[1] as Principal
    def logger = args[2]

    logger.info("Checking wildcard access {}", surrogate)
    return false
}

def canAuthenticate(Object... args) {
    def surrogate = args[0].toString()
    def principal = args[1] as Principal
    def service = args[2] as Service
    def logger = args[3]

    logger.info("Checking surrogate access for {}", surrogate)
    def accounts = getAccounts(principal.id, logger)
    return accounts.contains(surrogate)
}

def getAccounts(Object... args) {
    def user = args[0].toString()
    def logger = args[1]
    logger.info("Getting authorized accounts for {}", user)
    return []
}
```

{% include_cached casproperties.html properties="cas.authn.surrogate.groovy" %}
