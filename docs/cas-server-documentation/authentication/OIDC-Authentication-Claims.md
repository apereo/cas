---
layout: default
title: CAS - OpenID Connect Authentication
category: Protocols
---
{% include variables.html %}

# Claims - OpenID Connect Authentication

OpenID connect claims are treated as normal CAS attributes that need to
be [resolved, mapped and released](../integration/Attribute-Release-Policies.html).

<div class="alert alert-warning">
<strong>ID Token Claims</strong><p>
Per OpenID Connect specification, individual claims requested by OpenID scopes,
such as <code>profile</code>, <code>email</code>, etc. are only put into the OpenID 
Connect ID token when the response type is set to <code>id_token</code>. To assist with 
backward compatibility and non-complying application integrations, CAS provides options to force-include
claims in the ID token, though please note that this should be a last workaround as doing so most likely
is in violation of the OpenID Connect specification. Claims should be requested from 
the userinfo/profile endpoints in exchange for an access token as indicated by the appropriate response type.
</p></div>

## Configuration

{% include casproperties.html properties="cas.authn.oidc.core" %}

## Scope-based Claims

You may chain various attribute release policies that authorize claim release based on specific scopes:

```json
{
  "@class" : "org.apereo.cas.services.OidcRegisteredService",
  "clientId": "...",
  "clientSecret": "...",
  "serviceId" : "...",
  "name": "OIDC Test",
  "id": 10,
  "scopes" : [ "java.util.HashSet", 
    [ "openid", "profile", "email", "address", "phone", "offline_access", "displayName", "eduPerson" ]
  ]
}
```

Standard scopes that internally catalog pre-defined claims all belong to 
the namespace `org.apereo.cas.oidc.claims` and are described below:

| Policy                                              | Description
|-----------------------------------------------------|--------------------------------------------------------------------
| `o.a.c.o.c.OidcProfileScopeAttributeReleasePolicy`  | Release claims mapped to the spec-predefined `profile` scope.
| `o.a.c.o.c.OidcEmailScopeAttributeReleasePolicy`  | Release claims mapped to the spec-predefined `email` scope.
| `o.a.c.o.c.OidcAddressScopeAttributeReleasePolicy`  | Release claims mapped to the spec-predefined `address` scope.
| `o.a.c.o.c.OidcPhoneScopeAttributeReleasePolicy`  | Release claims mapped to the spec-predefined `phone` scope.
| `o.a.c.o.c.OidcCustomScopeAttributeReleasePolicy`  | Release claims mapped to the CAS-defined `custom` scope.
 
## Mapping Claims

Claims associated with a scope (i.e. `given_name` for `profile`) are fixed in 
the [OpenID specification](http://openid.net/specs/openid-connect-basic-1_0.html). In the 
event that custom arbitrary attributes should be mapped to claims, mappings can be defined in CAS 
settings to link a CAS-defined attribute to a fixed given scope. For instance, CAS configuration may 
allow the value of the attribute `sys_given_name` to be mapped and assigned to the claim `given_name` 
without having an impact on the attribute resolution configuration and all other CAS-enabled applications. 

If mapping is not defined, by default CAS attributes are expected to match claim names.

## User-Defined Scopes

Note that in addition to standard system scopes, you may define your own custom scope with a number of attributes within:

```json
{
  "@class" : "org.apereo.cas.services.OidcRegisteredService",
  "clientId": "...",
  "clientSecret": "...",
  "serviceId" : "...",
  "name": "OIDC Test",
  "id": 10,
  "scopes" : [ "java.util.HashSet", [ "displayName", "eduPerson" ] ]
}
```
 
These such as `displayName` above, get bundled into a `custom` scope which 
can be used and requested by services and clients.

If you however wish to define your custom scopes as an extension of what OpenID Connect defines
such that you may bundle attributes together, then you need to first register your `scope`,
define its attribute bundle and then use it a given service definition such as `eduPerson` above.
Such user-defined scopes are also able to override the definition of system scopes.

## Releasing Claims

Defined scopes for a given service definition control and build attribute release policies internally. Such attribute release
policies allow one to release standard claims, remap attributes to standard claims, or define custom claims and scopes altogether. 

It is also possible to define and use *free-form* attribute release policies outside 
the confines of a *scope* to freely build and release claims/attributes.  

For example, the following service definition will decide on relevant attribute release policies based on the semantics
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

To learn more about attribute release policies and the chain of 
command, please [see this guide](../integration/Attribute-Release-Policies.html).
