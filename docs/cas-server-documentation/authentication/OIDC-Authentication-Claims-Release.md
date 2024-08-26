---
layout: default
title: CAS - OpenID Connect Authentication
category: Protocols
---
{% include variables.html %}

# Releasing Claims - OpenID Connect Authentication

Defined scopes for a given service definition control and build attribute release policies internally. Such attribute release
policies allow one to release standard claims, remap attributes to standard claims, or define custom claims and scopes altogether. 

It is also possible to define and use *free-form* attribute release policies outside 
the confines of a *scope* to freely build and release claims/attributes.  

<div class="alert alert-warning">:warning: <strong>ID Token Claims</strong><p>
Per OpenID Connect specification, individual claims requested by OpenID scopes,
such as <code>profile</code>, <code>email</code>, etc. are only put into the OpenID 
Connect ID token when the response type is set to <code>id_token</code>. To assist with 
backward compatibility and non-complying application integrations, CAS provides options to force-include
claims in the ID token, though please note that this should be a last workaround as doing so most likely
is in violation of the OpenID Connect specification. Claims should be requested from 
the userinfo/profile endpoints in exchange for an access token as indicated by the appropriate response type.
</p></div>

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

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](../integration/Apache-Groovy-Scripting.html).
   
It is also possible to mix scopes and free-form policies together using the following short-hand:

```json
{
  "@class": "org.apereo.cas.services.OidcRegisteredService",
  "clientId": "client",
  "clientSecret": "secret",
  "serviceId": "^https://.+",
  "name": "Sample",
  "id": 1,
  "scopes" : [ "java.util.HashSet", [ "email", "profile", "openid" ] ],
  "supportedGrantTypes": [ "java.util.HashSet", [ "authorization_code" ] ],
  "supportedResponseTypes": [ "java.util.HashSet", [ "code" ] ],
  "attributeReleasePolicy": {
    "@class": "org.apereo.cas.oidc.claims.OidcScopeFreeAttributeReleasePolicy",
    "allowedAttributes" : [ "java.util.ArrayList", [ "system_type", "user_type" ] ]
  }
}
```
    
The above application definition is instructed to process claims assigned to standard scopes `email`, `profile` and `openid`,
and will also release `system_type` and `user_type` as claims regardless of the scopes requested by the relying party.

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
