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
    "structured": false,
    "trustFramework": "eidas"
  }
}
```

The following additional settings can be specified for a OpenID Connect attribute definitions:

| Name             | Description                                                                                                                                                                                                                                        |
|------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------||
| `structured`     | Default is `false`. Determines if the resulting attribute should be encoded as a hierarchical/structured attribute, activated only if the `name` assigned to the attribute definition indicates a hierarchical layout such as `parent.child.child` |
| `trustFramework` | Default is empty. Name of the [identity assurance trust framework](OIDC-Authentication-Identity-Assurance.html) that would force CAS to mark this claim as a verified claim.                                                                       |

To learn more about attribute definitions, please [see this guide](../integration/Attribute-Definitions.html).
