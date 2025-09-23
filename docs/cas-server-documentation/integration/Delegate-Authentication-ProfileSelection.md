---
layout: default
title: CAS - Delegate Authentication Profile Selection
category: Authentication
---

{% include variables.html %}

# Delegated Authentication Profile Selection

Delegated authentication flows can be customized to allow the end-user to select an authentication profile,
in cases where the user account produced by the identity provider can match multiple records
and is linked to multiple personas. When multiple matches are found, the CAS user interface allows the end-user to select
the appropriate profile with which authentication should resume. 
               
The credential resolution rules for the delegation flow are consulted using the following options.
    
{% tabs delegatedauthnprofileselection %}

{% tab delegatedauthnprofileselection LDAP %}

Candidate profiles after delegated authentication can be found inside an LDAP directory. There are options available to fetch
specific attributes from LDAP for each profile and the ability to specify the attribute which would be used the profile identifier.

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-ldap-core" %}

{% include_cached casproperties.html properties="cas.authn.pac4j.profile-selection.ldap[]" %}

{% endtab %}

{% tab delegatedauthnprofileselection <i class="fa fa-file-code px-1"></i>Groovy %}

{% include_cached casproperties.html properties="cas.authn.pac4j.profile-selection.groovy" %}

Profile selection rules can be supplied to CAS using an external Groovy script, whose outline should match the following:

```groovy
import org.apereo.cas.authentication.principal.*   
import org.apereo.cas.web.*
import org.pac4j.core.profile.*
import org.springframework.webflow.execution.*
import java.util.*

def run(Object[] args) {
    def (requestContext,clientCredentials,userProfile,logger) = args
    def profile = DelegatedAuthenticationCandidateProfile
        .builder()
        // build the result... 
        .build()
    return [profile]
}
```

The following parameters are passed to the script:

| Parameter           | Description                                                                                      |
|---------------------|--------------------------------------------------------------------------------------------------|
| `requestContext`    | `RequestContext` that represents the Spring Webflow execution context and runtime.               |
| `clientCredentials` | Represents CAS credential and payload received by CAS from the identity provider.                |
| `userProfile`       | Points to the *resolved* user profile from the identity provider in exchange for the credential. |
| `logger`            | The object responsible for issuing log messages such as `logger.info(...)`.                      |

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](../integration/Apache-Groovy-Scripting.html).

{% endtab %}

{% tab delegatedauthnprofileselection <i class="fa fa-code px-1"></i> Custom %}

If you wish to create your own profile resolution and selection strategy, you will need to
design a component and register it with CAS to handle the rendering of the user profile:

```java
@Bean
public DelegatedClientAuthenticationCredentialResolver myResolver() {
    return new MyResolver();
}
```

<div class="alert alert-info">:information_source: <strong>Usage</strong><p>
The name of the bean can be of your own choosing. You are also allowed to create multiple bean definitions
that handle different type of credentials with different resolution rules that may be 
executed using a specific order.</p></div>

The implementation body of the resolver is responsible for accepting a response that is produced by the identity provider.
This response that carries the initial user profile can then be processed to determine if it can be linked and matched
to multiple *internal* user accounts, or the response itself could be examined without external dependencies to determine
if the user account has multiple parallel profiles available via i.e. looking at a multi-valued attribute in the
response. Whatever the resolution rules may be, the end result of the implementation is expected to
produce a list of `DelegatedAuthenticationCandidateProfile` objects that represents various traits of the user profile.

{% endtab %}

{% endtabs %}
