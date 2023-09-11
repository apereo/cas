---
layout: default
title: CAS - OpenID Connect Authentication
category: Protocols
---
{% include variables.html %}

# Subject Identifier Claim - OpenID Connect Authentication

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
