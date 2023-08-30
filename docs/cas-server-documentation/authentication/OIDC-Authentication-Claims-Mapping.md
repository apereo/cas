---
layout: default
title: CAS - OpenID Connect Authentication
category: Protocols
---
{% include variables.html %}

# Mapping Claims - OpenID Connect Authentication

Claims associated with a scope (i.e. `given_name` for `profile`) are fixed in 
the [OpenID specification](http://openid.net/specs/openid-connect-basic-1_0.html). In the 
event that custom arbitrary attributes should be mapped to claims, mappings can be defined in CAS 
settings to link a CAS-defined attribute to a fixed given scope. For instance, CAS configuration may 
allow the value of the attribute `sys_given_name` to be mapped and assigned to the claim `given_name` 
without having an impact on the attribute resolution configuration and all other CAS-enabled applications. 

If mapping is not defined, by default CAS attributes are expected to match claim names.

Claim mapping rules that are defined in CAS settings are global and apply to all applications and requests. Once a claim is mapped
to an attribute (i.e. `preferred_username` to `uid`), this mapping rule will take over all claim processing rules and conditions.
     
## Mapping Claims Per Service

Claim mapping rules may also be defined for each application using the rules described below:

{% tabs oidcclaimmapping %}

{% tab oidcclaimmapping Standard Scopes %}

The configuration below will allow CAS to map the value of the `uid` attribute to the `preferred_username` claim that is constructed in response to
an authentication request from application `Sample`. The claim mapping rule here is exclusive to this application only, and does not affect
any other application or global mapping rule, if any.

```json
{
  "@class": "org.apereo.cas.services.OidcRegisteredService",
  "clientId": "client",
  "clientSecret": "secret",
  "serviceId": "^https://...",
  "name": "Sample",
  "id": 1,
  "scopes" : [ "java.util.HashSet", [ "openid", "profile" ] ],
  "attributeReleasePolicy": {
    "@class": "org.apereo.cas.oidc.claims.OidcProfileScopeAttributeReleasePolicy",
    "claimMappings" : {
      "@class" : "java.util.TreeMap",
      "preferred_username" : "uid"
    }
  }
}
```

{% endtab %}

{% tab oidcclaimmapping User-Defined Scopes %}

The configuration below will allow CAS to map the value of the `entitlements` claim to the outcome of the inline Groovy script,
when processing the rules for the `MyCustomScope` scope. 

```json
{
  "@class": "org.apereo.cas.services.OidcRegisteredService",
  "clientId": "client",
  "clientSecret": "secret",
  "serviceId": "^https://...",
  "name": "Sample",
  "id": 1,
  "scopes" : [ "java.util.HashSet", [ "openid", "profile", "MyCustomScope" ] ],
  "attributeReleasePolicy": {
    "@class": "org.apereo.cas.services.ChainingAttributeReleasePolicy",
    "policies": [
      "java.util.ArrayList",
      [
        {
          "@class": "org.apereo.cas.oidc.claims.OidcCustomScopeAttributeReleasePolicy",
          "order": 1,
          "scopeName": "MyCustomScope",
          "allowedAttributes" : [ "java.util.ArrayList", [ "entitlements" ] ],
          "claimMappings" : {
            "@class" : "java.util.TreeMap",
            "entitlements" : "groovy { return ['A', 'B'] }"
          }
        }
      ]
    ]
  }
}
```
  
The inline script receives the following parameters for its execution:

| Policy       | Description                                                                                            |
|--------------|--------------------------------------------------------------------------------------------------------|
| `context`    | Attribute release execution context that carries references to the principal, registered service, etc. |
| `attributes` | `Map` of attributes that are currently resolved.                                                       |
| `logger`     | The object responsible for issuing log messages such as `logger.info(...)`.                            |
      
Note that the outcome of the script execution must be a `List` of a values.

{% endtab %}

{% endtabs %}

## Releasing Claims

Defined scopes for a given service definition control and build attribute release policies internally. Such attribute release
policies allow one to release standard claims, remap attributes to standard claims, or define custom claims and scopes altogether. 

It is also possible to define and use *free-form* attribute release policies outside 
the confines of a *scope* to freely build and release claims/attributes.  

{% tabs oidcclaimrelease %}

{% tab oidcclaimrelease Standard %}

The following service definition will decide on relevant attribute release policies based on the semantics
of the scopes `profile` and `email`. There is no need to design or list individual claims as CAS will auto-configure
the relevant attribute release policies:

```json
{
  "@class" : "org.apereo.cas.services.OidcRegisteredService",
  "clientId": "client",
  "clientSecret": "secret",
  "serviceId" : "...",
  "name": "OIDC",
  "id": 1,
  "scopes" : [ "java.util.HashSet",
    [ "openid", "profile", "email" ]
  ]
}
```

{% endtab %}

{% tab oidcclaimrelease Scope Free %}

A *scope-free* attribute release policy may just as equally apply, allowing one in
the following example to release `userX` as a *claim*:

```json
{
  "@class" : "org.apereo.cas.services.OidcRegisteredService",
  "clientId": "client",
  "clientSecret": "secret",
  "serviceId" : "...",
  "name": "OIDC",
  "id": 1,
  "attributeReleasePolicy" : {
    "@class" : "org.apereo.cas.services.ReturnMappedAttributeReleasePolicy",
    "allowedAttributes" : {
      "@class" : "java.util.TreeMap",
      "userX" : "groovy { return attributes['uid'].get(0) + '-X' }"
    }
  }
}
```

A scope-free attribute release policy is activated when the service definition does not specify any scopes, or the only scope that
the service definition contains is the `openid` scope. A *scope-free* attribute release policy has the ability to process release claims
regardless of the requested scopes, which may prove useful in scenarios where a relying party needs to receive claims and yet
does not correctly or sufficiently specify a scope in authorization requests.

<div class="alert alert-info">:information_source: <strong>Usage</strong><p>You should consider using a scope-free attribute release policy
only in very advanced and challenging use cases, typically to make a rather difficult client application integration work.</p>
</div>

{% endtab %}

{% tab oidcclaimrelease Mixed %}

It is also possible to mix *free-form* release policies with those that operate
based on a scope by chaining such policies together. For example, the below policy
allows the release of `user-x` as a claim, as well as all claims assigned
and internally defined for the standard `email` scope.

```json
{
  "@class": "org.apereo.cas.services.OidcRegisteredService",
  "clientId": "client",
  "clientSecret": "secret",
  "serviceId": "...",
  "name": "OIDC",
  "id": 10,
  "attributeReleasePolicy": {
    "@class": "org.apereo.cas.services.ChainingAttributeReleasePolicy",
    "policies": [
      "java.util.ArrayList",
      [
        {
          "@class" : "org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy",
          "allowedAttributes" : [ "java.util.ArrayList", [ "cn", "uid", "givenName" ] ],
          "order": 0  
        },
        {
          "@class": "org.apereo.cas.services.ReturnMappedAttributeReleasePolicy",
          "allowedAttributes": {
            "@class": "java.util.TreeMap",
            "user-x": "groovy { return attributes['uid'].get(0) + '-X' }"
          },
          "order": 1
        },
        {
          "@class": "org.apereo.cas.oidc.claims.OidcEmailScopeAttributeReleasePolicy",
          "order": 2
        }
      ]
    ]
  }
}
```

{% endtab %}

{% tab oidcclaimrelease Claim Filtering %}

It is possible to control the release of standard claims, i.e. `name`, that are connect to a standard scope, such as `profile`.
Typically when the release policy references a standard scope, all claims available and resolved that belong to that scope
are then released to the relying party. The configuration below allows direct and fine-tuned control over the set of claims
that could be released as part of the larger claim bundle that is tied to a standard scope.

```json
{
  "@class": "org.apereo.cas.services.OidcRegisteredService",
  "clientId": "client",
  "clientSecret": "secret",
  "serviceId": "...",
  "name": "Sample",
  "id": 1,
  "supportedGrantTypes": [ "java.util.HashSet", [ "authorization_code" ]],
  "supportedResponseTypes": [ "java.util.HashSet", [ "code" ]],
  "attributeReleasePolicy": {
    "@class": "org.apereo.cas.services.ChainingAttributeReleasePolicy",
    "policies": [ "java.util.ArrayList",
      [
        {
          "@class": "org.apereo.cas.oidc.claims.OidcProfileScopeAttributeReleasePolicy",
          "allowedAttributes" : [ "java.util.ArrayList", [ "locale", "name" ] ]
        },
        {
          "@class": "org.apereo.cas.oidc.claims.OidcEmailScopeAttributeReleasePolicy",
          "allowedAttributes" : [ "java.util.ArrayList", [ "email" ] ]
        }
      ]
    ]
  }
}
```

If all claims available to the `profile` and `email` scopes are resolved and available to CAS for attribute release,
the configuration above will only authorize the release of `locale`, `name` and `email` out of the entire set of
available claims.

{% endtab %}

{% endtabs %}

To learn more about attribute release policies and the chain of 
command, please [see this guide](../integration/Attribute-Release-Policies.html).

## Pairwise Identifiers

When `pairwise` subject type is used, CAS will calculate a unique `sub` value for each sector identifier. This identifier
should not be reversible by any party other than CAS and is somewhat akin to CAS generating persistent anonymous user
identifiers. Each value provided to every relying party is different so as not
to enable clients to correlate the user's activities without permission.

```json
{
  "@class" : "org.apereo.cas.services.OidcRegisteredService",
  "clientId": "client",
  "clientSecret": "secret",
  "serviceId" : "^<https://the-redirect-uri>",
  "subjectType": "pairwise",
  "usernameAttributeProvider" : {
    "@class" : "org.apereo.cas.services.PairwiseOidcRegisteredServiceUsernameAttributeProvider",
    "persistentIdGenerator" : {
      "@class" : "org.apereo.cas.authentication.principal.OidcPairwisePersistentIdGenerator",
      "salt" : "aGVsbG93b3JsZA=="
    }
  }
}
```

## Subject Identifier Claim

To control and modify the value of the `sub` claim for each OpenID Connect relying party, you may change the application 
definition to return an attribute that is already resolved for the principal as the `sub` claim value for this service. 

```json
{
  "@class" : "org.apereo.cas.services.OidcRegisteredService",
  "clientId": "client",
  "clientSecret": "secret",
  "serviceId" : "^<https://the-redirect-uri>",
  "scopes" : [ "java.util.HashSet", [ "openid", "profile" ] ]
  "usernameAttributeProvider" : {
    "@class" : "org.apereo.cas.services.PrincipalAttributeRegisteredServiceUsernameProvider",
    "usernameAttribute" : "cn"
  }
}
```

In general, all other constructs available to CAS that are [described here](../integration/Attribute-Release-PrincipalId.html) which 
control the principal identifier that is shared with a client application may also be used to control the `sub` claim.
