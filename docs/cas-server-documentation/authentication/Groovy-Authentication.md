---
layout: default
title: CAS - Groovy Authentication
category: Authentication
---
{% include variables.html %}


# Groovy Authentication

Verify and authenticate credentials using Groovy scripts. The task of credential verification, principal transformation,
handling password policy and all other related matters are the sole responsibility of the Groovy script.

Support is enabled by including the following dependency in the WAR overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-generic" %}

{% include casproperties.html properties="cas.authn.groovy"  %}

The Groovy script may be designed as:

```groovy
import org.apereo.cas.authentication.*
import org.apereo.cas.authentication.credential.*
import org.apereo.cas.authentication.metadata.*

import javax.security.auth.login.*

def authenticate(final Object... args) {
    def authenticationHandler = args[0]
    def credential = args[1]
    def servicesManager = args[2]
    def principalFactory = args[3]
    def logger = args[4]              

    /*
     * Figure out how to verify credentials...
     */
    if (authenticationWorksCorrectly()) {
        def principal = principalFactory.createPrincipal(credential.username);
        return new DefaultAuthenticationHandlerExecutionResult(authenticationHandler,
                new BasicCredentialMetaData(credential),
                principal,
                new ArrayList<>(0));
    }
    throw new FailedLoginException();
}

def supportsCredential(final Object... args) {
    def credential = args[0]
    def logger = args[1]
    return credential != null
}

def supportsCredentialClass(final Object... args) {
    def credentialClazz = args[0]
    def logger = args[1]
    return credentialClazz == UsernamePasswordCredential.class
}
```
