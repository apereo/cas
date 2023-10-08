---
layout: default
title: CAS - SAML2 NameID Configuration
category: Protocols
---
{% include variables.html %}

# SAML2 Logout & SLO

SLO is a mechanism that allows a user to log out of one service 
provider and be simultaneously logged out of all other service providers and the identity 
provider. SLO ensures that a user's session is terminated across all participating entities 
when they log out of one service.

The following endpoints in CAS support SAML2 SLO operations:

| Endpoint                          | Description                                 |
|-----------------------------------|---------------------------------------------|
| `/idp/profile/SAML2/POST/SLO`     | Handles SLO requests for `POST` bindings.     |
| `/idp/profile/SAML2/Redirect/SLO` | Handles SLO requests for `Redirect` bindings. |
             
You will also find the above endpoints in the SAML2 identity provider metadata generated and managed by CAS:

```xml
<IDPSSODescriptor ...>
    <SingleLogoutService Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST" 
        Location="https://sso.example.org/cas/idp/profile/SAML2/POST/SLO"/>
        
    <SingleLogoutService Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect" 
        Location="https://sso.example.org/cas/idp/profile/SAML2/Redirect/SLO" />
</IDPSSODescriptor>    
```

{% include_cached casproperties.html properties="cas.authn.saml-idp.logout" %}

## Logout Response

SAML2 logout response bindings are decided based on the following rules:

- The binding can be explicitly defined and overridden for a service provider:

```json
{
  "@class" : "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId" : "https://sp.example.org",
  "name" : "SAML",
  "id" : 1,
  "metadataLocation" : "/path/to/sp/metadata.xml",
  "logoutResponseBinding": "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST"
}
```
- The service provider metadata is consulted and CAS may pick the `SingleLogoutService` defined in the metadata. Order of `SingleLogoutService` elements in the metadata is important and CAS typically will choose the first entry.
- A global binding value can then be chosen and defined in CAS configuration properties.

```xml
<md:SingleLogoutService Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect" 
    Location="http://sp.example.org/slo/redirect"/>
<md:SingleLogoutService Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST" 
    Location="http://sp.example.org/slo/post"/>
```

- CAS may choose `urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect` as the fallback binding.
