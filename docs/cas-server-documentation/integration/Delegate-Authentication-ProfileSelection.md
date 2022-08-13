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
               
The credential resolution rules for the delegation flow are consulted using the following bean definition that are expected
to be implemented by the CAS operator:

```java
@Bean
public DelegatedClientAuthenticationCredentialResolver myResolver() {
    ...
}
```

<div class="alert alert-info"><strong>Usage</strong><p>
The name of the bean is irrelevant and can be of your own choosing. You are also allowed to create multiple bean definitions
that handle different type of credentials with different resolution rules that may be executed using a specific order.</p></div>

The implementation body of the resolver is responsible for accepting a response that is produced by the external identity provider.
This response that carries the initial user profile can then be processed to determine if it can be linked and matched
to multiple *internal* user accounts, or the response itself could be examined without external dependencies to determine 
if the user account has mutiple parallel profiles available via i.e. looking at a multi-valued attribute in the 
response. Whatever the resolution rules may be, the end result of the implementation is expected to 
produce a list of `DelegatedAuthenticationCandidateProfile` objects that represents various traits of the user profile. 
