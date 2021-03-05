---
layout: default
title: CAS - Delegate Authentication
category: Authentication
---

{% include variables.html %}

# OpenID Connect Generic

For an overview of the delegated authentication flow, please [see this guide](Delegate-Authentication.html).

{% include casproperties.html properties="cas.authn.pac4j.oidc[].generic" %}

## Per Service Customizations

Th configuration for the external OpenID Connect identity provider is typically done at build time
via CAS configuration settings and applies to all applications and relying parties. You may override
certain aspects this configuration on a per application basis by assigning
dedicated [properties to the service definition](../services/Configuring-Service-Custom-Properties.html).

The following properties are available as overrides:

| Property                              | Value(s)
|---------------------------------------|---------------------------------
| `max_age`                             | `Integer`
| `scope`                               | `String`
| `response_type`                       | `String`
| `response_mode`                       | `String`

A sample JSON file follows:

```json
{
  "@class" : "org.apereo.cas.services.RegexRegisteredService",
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
