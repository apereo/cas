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

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-generic" %}

{% include_cached casproperties.html properties="cas.authn.groovy"  %}

The Groovy script may be designed as:

```groovy
import org.apereo.cas.authentication.*
import org.apereo.cas.authentication.credential.*
import org.apereo.cas.authentication.metadata.*

import javax.security.auth.login.*

def authenticate(final Object... args) {
    def (authenticationHandler,credential,servicesManager,principalFactory,logger) = args

    /*
     * Figure out how to verify credentials...
     */
    if (authenticationWorksCorrectly()) {
        def principal = principalFactory.createPrincipal(credential.username);
        return new DefaultAuthenticationHandlerExecutionResult(authenticationHandler,
            credential, principal, new ArrayList<>());
    }
    throw new FailedLoginException();
}

def supportsCredential(final Object... args) {
    def (credential,logger) = args
    return credential != null
}

def supportsCredentialClass(final Object... args) {
    def (credentialClazz,logger) = args
    return credentialClazz == UsernamePasswordCredential.class
}
```

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](../integration/Apache-Groovy-Scripting.html).
