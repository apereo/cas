---
layout: default
title: CAS - OpenID Connect Authentication
category: Protocols
---
{% include variables.html %}

# OpenID Connect Claim Definitions

Attribute definitions that specifically apply to the release of attributes as part of OpenID Connect responses can be
defined using the `OidcAttributeDefinition`. Defining an attribute with this definition does not
prevent it from being released by other protocols.

```json
{
  "@class": "java.util.TreeMap",
  "mail": {
    "@class": "org.apereo.cas.oidc.claims.OidcAttributeDefinition",
    "key": "mail",
    "singleValue": true
  }
}
```

The following additional settings can be specified for a Saml IdP attribute definition:

| Name          | Description                                                                                                               |
|---------------|---------------------------------------------------------------------------------------------------------------------------|
| `singleValue` | Default is `false`. Determines if the attribute should be produced as a single-value claim if it has only a single value. |

To learn more about attribute definitions, please [see this guide](../integration/Attribute-Definitions.html).
