---
layout: default
title: CAS - SAML2 Authentication
category: Protocols
---
{% include variables.html %}

# SAML2 Authentication - Unsolicited SSO

SAML2 IdP `Unsolicited/SSO` profile, also known as *IdP Initiated*, supports the following parameters:

| Parameter    | Description                                                    |
|--------------|----------------------------------------------------------------|
| `providerId` | **Required**. Entity ID of the service provider.               |
| `shire`      | Optional. Response location (ACS URL) of the service provider. |
| `target`     | Optional. Relay state.                                         |
| `time`       | Optional. Skew the authentication request.                     |
  
A typical request to the CAS would look like this:

```bash
GET https://sso.example.org/cas/idp/profile/SAML2/Unsolicited/SSO \
    ?providerId=https://sp.example.org/saml2 \
    &shire=my-relay-state-here
```
