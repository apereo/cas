---
layout: default
title: CAS - Delegate Authentication
category: Authentication
---

{% include variables.html %}

# OpenID Connect Generic

For an overview of the delegated authentication flow, please [see this guide](Delegate-Authentication.html).

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-pac4j-oidc" %}

## Configuration

{% include_cached casproperties.html properties="cas.authn.pac4j.oidc[].generic" excludes=".federation" %}

## Federation configuration

The federation nature of a delegated OIDC client must be explicitely enabled via CAS configuration. 

Federation support translates into two aspects:

1. Exposing the entity statement of the OIDC client (The CAS server itself)
2. Resolving the trust chain to retrieve the target provider via the trust anchors.

{% include_cached casproperties.html properties="cas.authn.pac4j.oidc[].generic.federation" %}

For exposing the entity statement, a JWKS must be defined as a resource (file or URL) with an optional key identifier. The 
entity statement is exposed on the `/rp/{clientName}/.well-known/openid-federation` URL of the CAS server.

For resolving the trust chain and authenticating on a target provider, the OP issuer 
must be defined as the `targetOp` property along with one or more 
trust anchors.

{% include_cached casproperties.html properties="cas.authn.pac4j.oidc[].generic.federation" %}

## Per Service Customizations

Th configuration for the external OpenID Connect identity provider is typically done at build time
via CAS configuration settings and applies to all applications and relying parties. You may override
certain aspects this configuration on a per application basis by assigning
dedicated [properties to the service definition](../services/Configuring-Service-Custom-Properties.html).

{% include_cached registeredserviceproperties.html groups="DELEGATED_AUTHN,DELEGATED_AUTHN_OIDC" %}

A sample JSON file follows:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^https://app.example.org",
  "name" : "Example",
  "id" : 1,
  "properties" : {
    "@class" : "java.util.HashMap",
    "max_age" : {
      "@class" : "org.apereo.cas.services.DefaultRegisteredServiceProperty",
      "values" : [ "java.util.HashSet", [ "1000" ] ]
    },
    "scope" : {
      "@class" : "org.apereo.cas.services.DefaultRegisteredServiceProperty",
      "values" : [ "java.util.HashSet", [ "openid profile" ] ]
    }
  }
}
```

See [registered service properties](../services/Configuring-Service-Custom-Properties.html) for more details.
