---
layout: default
title: CAS - OpenID Connect Authentication
category: Protocols
---
{% include variables.html %}

# OpenID Connect Authentication JWKS Storage - Groovy

Keystore generation can be outsourced to an external Groovy script whose body should be defined as such: 

```groovy
import org.apereo.cas.oidc.jwks.*
import org.jose4j.jwk.*

def run(Object[] args) {
    def (logger) = args
    logger.info("Generating JWKS for CAS...")
    def jsonWebKeySet = "{ \"keys\": [...] }"
    return jsonWebKeySet
}

def store(Object[] args) {
    def (jwks,logger) = args
    logger.info("Storing JWKS for CAS...")
    return jwks
}

def find(Object[] args) {
    def (logger) = args
    logger.info("Looking up JWKS...")
    return new JsonWebKeySet(...)
}
```

{% include_cached casproperties.html properties="cas.authn.oidc.jwks.groovy" %}

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](../integration/Apache-Groovy-Scripting.html).
