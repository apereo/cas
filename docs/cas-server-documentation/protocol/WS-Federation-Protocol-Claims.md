---
layout: default
title: CAS - CAS WS Federation Protocol
category: Protocols
---

{% include variables.html %}

# Claims - WS Federation Protocol

Attribute filtering and release policies are defined per 
relying party. See [this guide](../integration/Attribute-Release-Policies.html) for more info.

The following standard claims are supported by CAS for release:

| Claim                           | Description
|---------------------------------|-----------------------------------------------------------------------------------
| `EMAIL_ADDRESS_2005`            | `http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress`
| `EMAIL_ADDRESS`                 | `http://schemas.xmlsoap.org/claims/EmailAddress`
| `GIVEN_NAME`                    | `http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname`
| `NAME`                          | `http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name`
| `USER_PRINCIPAL_NAME_2005`      | `http://schemas.xmlsoap.org/ws/2005/05/identity/claims/upn`
| `USER_PRINCIPAL_NAME`           | `http://schemas.xmlsoap.org/claims/UPN`
| `COMMON_NAME`                   | `http://schemas.xmlsoap.org/claims/CommonName`
| `GROUP`                         | `http://schemas.xmlsoap.org/claims/Group`
| `MS_ROLE`                       | `http://schemas.microsoft.com/ws/2008/06/identity/claims/role`
| `ROLE`                          | `http://schemas.xmlsoap.org/ws/2005/05/identity/claims/role`
| `SURNAME`                       | `http://schemas.xmlsoap.org/ws/2005/05/identity/claims/surname`
| `PRIVATE_ID`                    | `http://schemas.xmlsoap.org/ws/2005/05/identity/claims/privatepersonalidentifier`
| `NAME_IDENTIFIER`               | `http://schemas.xmlsoap.org/ws/2005/05/identity/claims/nameidentifier`
| `AUTHENTICATION_METHOD`         | `http://schemas.microsoft.com/ws/2008/06/identity/claims/authenticationmethod`
| `DENY_ONLY_GROUP_SID`           | `http://schemas.xmlsoap.org/ws/2005/05/identity/claims/denyonlysid`
| `DENY_ONLY_PRIMARY_SID`         | `http://schemas.microsoft.com/ws/2008/06/identity/claims/denyonlyprimarysid`
| `DENY_ONLY_PRIMARY_GROUP_SID`   | `http://schemas.microsoft.com/ws/2008/06/identity/claims/denyonlyprimarygroupsid`
| `GROUP_SID`                     | `http://schemas.microsoft.com/ws/2008/06/identity/claims/groupsid`
| `PRIMARY_GROUP_SID`             | `http://schemas.microsoft.com/ws/2008/06/identity/claims/primarygroupsid`
| `PRIMARY_SID`                   | `http://schemas.microsoft.com/ws/2008/06/identity/claims/primarysid`
| `WINDOWS_ACCOUNT_NAME`          | `http://schemas.microsoft.com/ws/2008/06/identity/claims/windowsaccountname`
| `PUID`                          | `http://schemas.xmlsoap.org/claims/PUID`

The attribute release policy assigned to relying parties and 
services is able to link a given standard claim and map it to an attribute
that should be already available. The configuration looks as such:

```json
{
  "@class" : "org.apereo.cas.ws.idp.services.WSFederationRegisteredService",
  "serviceId" : "https://wsfed.example.org/.+",
  "realm" : "urn:wsfed:example:org:sampleapplication",
  "name" : "WSFED",
  "id" : 1,
  "attributeReleasePolicy" : {
    "@class" : "org.apereo.cas.ws.idp.services.WSFederationClaimsReleasePolicy",
    "allowedAttributes" : {
      "@class" : "java.util.TreeMap",
      "GIVEN_NAME" : "givenName"
    }
  }
}
```

The above snippet allows CAS to release the 
claim `http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname` whose value
is identified by the value of the `givenName` attribute that is already retrieved for the authenticated principal.

Attributes authorized and allowed for release by this policy may not necessarily be available
as resolved principal attributes and can be resolved on the fly dynamically
using the [attribute definition store](../integration/Attribute-Definitions.html).

## Inline Groovy Claims

Claims may produce their values from an inline Groovy 
script. As an example, the claim `EMAIL_ADDRESS_2005` may be constructed 
as a dynamic attribute whose value is determined by the inline Groovy script attribute and the `cn` attribute:

```json
{
  "@class" : "org.apereo.cas.services.RegexRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 300,
  "attributeReleasePolicy" : {
    "@class" : "org.apereo.cas.ws.idp.services.WSFederationClaimsReleasePolicy",
    "allowedAttributes" : {
      "@class" : "java.util.TreeMap",
      "EMAIL_ADDRESS_2005" : "groovy { return attributes['cn'].get(0) + '@example.org' }"
    }
  }
}
```

## File-based Groovy Claims

Claims may produce their values from an external Groovy 
script. As an example, the claim `EMAIL_ADDRESS_2005` may be constructed 
as a dynamic attribute whose value is determined by the Groovy script attribute and the `cn` attribute:

```json
{
  "@class" : "org.apereo.cas.services.RegexRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 300,
  "attributeReleasePolicy" : {
    "@class" : "org.apereo.cas.ws.idp.services.WSFederationClaimsReleasePolicy",
    "allowedAttributes" : {
      "@class" : "java.util.TreeMap",
      "EMAIL_ADDRESS_2005" : "file:/path/to/script.groovy"
    }
  }
}
```

The configuration of this component qualifies to use 
the [Spring Expression Language](../configuration/Configuration-Spring-Expressions.html) syntax. The script 
itself may have the following outline:

```groovy
def run(final Object... args) {
    def attributes = args[0]
    def logger = args[1]

    logger.info "Attributes currently resolved: ${attributes}"
    return [attributes["cn"][0] + "@example.org"]
}
```

## Custom Claims

You may also decide to release non-standard claims as part of a custom 
namespace. For example, the below snippet allows CAS to release the 
claim `https://github.com/apereo/cas/employeeNumber` whose value is 
identified by the value of the `personSecurityId` attribute that is 
already retrieved for the authenticated principal.

```json
{
  "@class" : "org.apereo.cas.ws.idp.services.WSFederationRegisteredService",
  "serviceId" : "https://wsfed.example.org/.+",
  "realm" : "urn:wsfed:example:org:sampleapplication",
  "name" : "WSFED",
  "id" : 1,
  "attributeReleasePolicy" : {
    "@class" : "org.apereo.cas.ws.idp.services.CustomNamespaceWSFederationClaimsReleasePolicy",
    "namespace": "https://github.com/apereo/cas",
    "allowedAttributes" : {
      "@class" : "java.util.TreeMap",
      "employeeNumber" : "personSecurityId"
    }
  }
}
```

Attributes authorized and allowed for release by this policy may not necessarily be available
as resolved principal attributes and can be resolved on the fly dynamically
using the [attribute definition store](../integration/Attribute-Definitions.html).
