---
layout: default
title: CAS - SAML2 NameID Configuration
category: Protocols
---
{% include variables.html %}

# SAML2 NameID Selection

Each service may specify a required Name ID format. If left undefined, the metadata
will be consulted to find the right format.
The Name ID value is always the authenticated user that is designed to be returned
to this service. In other words, if you
decide to configure CAS to return a particular attribute as
[the authenticated user name for this service](../integration/Attribute-Release-PrincipalId.html),
that value will then be used to construct the Name ID along with the right format.

## Examples

The following service definition instructs CAS to use the `urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress` 
as the final Name ID format, and use the `mail` attribute value as the final Name ID value.

```json
{
  "@class": "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId": "the-entity-id-of-the-sp",
  "name": "SAML Service",
  "metadataLocation": "/path/to/sp-metadata.xml",
  "id": 1,
  "requiredNameIdFormat": "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress",
  "usernameAttributeProvider" : {
    "@class" : "org.apereo.cas.services.PrincipalAttributeRegisteredServiceUsernameProvider",
    "usernameAttribute" : "mail",
  }
}
```

The following service definition instructs CAS to use the `urn:oasis:names:tc:SAML:2.0:attrname-format:unspecified`
as the final Name ID format, and use the `sysid` attribute value and the scope `example.org`. The final Name ID value would then
be constructed as `<sysid-attribute-value>@example.org`.

```json
{
  "@class": "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId": "the-entity-id-of-the-sp",
  "name": "SAML Service",
  "metadataLocation": "/path/to/sp-metadata.xml",
  "id": 1,
  "requiredNameIdFormat": "urn:oasis:names:tc:SAML:2.0:attrname-format:unspecified",
  "usernameAttributeProvider" : {
    "@class" : "org.apereo.cas.services.PrincipalAttributeRegisteredServiceUsernameProvider",
    "usernameAttribute" : "sysid",
    "scope": "example.org"
  }
}
```

The following service definition instructs CAS to use the `urn:oasis:names:tc:SAML:2.0:nameid-format:transient` as the final Name ID format,
and use the `cn` attribute value in upper-case as the final Name ID value, skipping the generation of transient value per the required format.

```json
{
  "@class": "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId": "the-entity-id-of-the-sp",
  "name": "SAML Service",
  "metadataLocation": "/path/to/sp-metadata.xml",
  "id": 1,
  "requiredNameIdFormat": "urn:oasis:names:tc:SAML:2.0:nameid-format:transient",
  "skipGeneratingTransientNameId" : true,
  "usernameAttributeProvider" : {
    "@class" : "org.apereo.cas.services.PrincipalAttributeRegisteredServiceUsernameProvider",
    "usernameAttribute" : "cn",
    "canonicalizationMode" : "UPPER"
  }
}
```

The following service definition instructs CAS to use the `cn` attribute value to create a persistent Name ID.

```json
{
  "@class": "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId": "the-entity-id-of-the-sp",
  "name": "SAML Service",
  "metadataLocation": "/path/to/sp-metadata.xml",
  "id": 1,
  "requiredNameIdFormat": "urn:oasis:names:tc:SAML:2.0:nameid-format:persistent",
  "usernameAttributeProvider" : {
    "@class" : "org.apereo.cas.services.AnonymousRegisteredServiceUsernameAttributeProvider",
    "persistentIdGenerator" : {
      "@class" : "org.apereo.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator",
      "salt" : "aGVsbG93b3JsZA==",
      "attribute": "cn"
    }
  }
}
```
