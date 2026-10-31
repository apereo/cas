---
layout: default
title: CAS - SAML2 Authentication - Unsolicited SSO
category: Protocols
---
{% include variables.html %}

# SAML2 Authentication - Unsolicited SSO

With IdP-initiated (unsolicited) SSO, CAS as a SAML2 identity provider starts the login and sends an assertion to a service provider that did not send an authentication request first.

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
    &target=my-relay-state-here
```

CAS builds the authentication request itself, for a service provider whose metadata it knows and for an assertion
consumer service that the metadata lists. That request is not signed, and it is not checked for a signature even when the
metadata of the service provider asks for signed authentication requests. Other request parameters are passed on to the
login flow.
