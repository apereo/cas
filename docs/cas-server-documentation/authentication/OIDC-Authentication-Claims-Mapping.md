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
| `attributes` | `Map` of attributes that are currently resolved.                                                       |
| `context`    | Attribute release execution context that carries references to the principal, registered service, etc. |
| `claim`      | The claim name that is being remapped, i.e. `entitlements`.                                            |
| `logger`     | The object responsible for issuing log messages such as `logger.info(...)`.                            |
      

You may also choose to use an exteral Groovy script instead, in which case the script may be defined as follows:

```groovy
def run(Object[] args) {
    def (attributes, context, claim, logger) = args
    // Perfom logic to build claim value
    return []
}
```

Note that the outcome of the script execution in all scenarios must be a `List` of a values.

{% endtab %}

{% endtabs %}

## Releasing Claims

Please see [this guide](OIDC-Authentication-Claims-Release.html) to learn more.

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
