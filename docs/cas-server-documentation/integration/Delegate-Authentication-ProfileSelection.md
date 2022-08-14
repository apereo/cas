---
layout: default
title: CAS - Delegate Authentication Profile Selection
category: Authentication
---

{% include variables.html %}

# Delegated Authentication Profile Selection

Delegated authentication flows can be customized to allow the end-user to select an authentication profile,
in cases where the user account produced by the identity provider can match multiple records
and is linked to multiple personas. When mutiple matches are found, the CAS user interface allows the end-user to select
the appropriate profile with which authentication should resume. 
               
The credential resolution rules for the delegation flow are consulted using the following options.

## Groovy
   
{% include_cached casproperties.html properties="cas.authn.pac4j.profile-selection.groovy" %}

Profile selection rules can be supplied to CAS using an external Groovy script, whose outline should match the following:

```groovy
import org.apereo.cas.authentication.principal.*
import org.apereo.cas.web.*
import org.pac4j.core.profile.*
import org.springframework.webflow.execution.*
import java.util.*

def run(Object[] args) {
    def requestContext = args[0] as RequestContext
    def clientCredentials = args[1] as ClientCredential
    def userProfile = (args[2] as Optional<UserProfile>).get()
    def logger = args[3]
    
    def profile = DelegatedAuthenticationCandidateProfile
        .builder()
        // build the result... 
        .build()
    return [profile]
}
```

The following parameters are passed to the script:

| Parameter             | Description
|---------------------------------------------------------------------------------------------------------
| `requestContext`      | `RequestContext` that represents the Spring Webflow execution context and runtime.
| `clientCredentials`   | Represents CAS credential and payload received by CAS from the identity provider.
| `userProfile`         | Points to the *resolved* user profile from the identity provider in exchange for the credential.
| `logger`              | The object responsible for issuing log messages such as `logger.info(...)`.

## Custom

If you wish to create your own profile resolution and selection strategy, you will need to
design a component and register it with CAS to handle the rendering of the user profile:

```java
@Bean
public DelegatedClientAuthenticationCredentialResolver myResolver() {
    return new MyResolver();
}
```

<div class="alert alert-info"><strong>Usage</strong><p>
The name of the bean can be of your own choosing. You are also allowed to create multiple bean definitions
that handle different type of credentials with different resolution rules that may be 
executed using a specific order.</p></div>

The implementation body of the resolver is responsible for accepting a response that is produced by the identity provider.
This response that carries the initial user profile can then be processed to determine if it can be linked and matched
to multiple *internal* user accounts, or the response itself could be examined without external dependencies to determine
if the user account has mutiple parallel profiles available via i.e. looking at a multi-valued attribute in the
response. Whatever the resolution rules may be, the end result of the implementation is expected to
produce a list of `DelegatedAuthenticationCandidateProfile` objects that represents various traits of the user profile.
