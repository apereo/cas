---
layout: default
title: CAS - OpenID Connect Authentication
category: Protocols
---
{% include variables.html %}

# OpenID Connect - Client-Initiated Backchannel Authentication (CIBA)

Client-Initiated Backchannel Authentication (CIBA) is a new authentication flow in which relying parties, that can obtain a 
valid identifier for the user they want to authenticate, will be able to initiate an interaction flow to 
authenticate their users without having end-user interaction from the consumption device. The flow involves direct 
communication from the Client to CAS without redirect through the user's browser.

- The Client shall make an "HTTP POST" request to the Backchannel Authentication Endpoint to ask for end-user authentication.
- CAS will respond immediately with a unique identifier that identifies that authentication while it tries to authenticate the user in the background.
- The Client will receive the ID Token, Access Token, and optionally Refresh Token through either the Poll, Ping, or Push modes.

Please study [the specification](https://openid.net/specs/openid-client-initiated-backchannel-authentication-core-1_0.html) to learn more.

## Configuration

{% include_cached casproperties.html properties="cas.authn.oidc.ciba" %}

## Applications

Applications that wish to take advantage of CIBA can be registered with CAS:

```json
{
  "@class": "org.apereo.cas.services.OidcRegisteredService",
  "clientId": "client-id",
  "clientSecret": "secret",
  "serviceId": "^https://app.example.org/oidc",
  "name": "MyApplication",
  "id": 1,
  "supportedGrantTypes": [ "java.util.HashSet", [ "urn:openid:params:grant-type:ciba" ] ],
  "backchannelTokenDeliveryMode": "push",
  "backchannelClientNotificationEndpoint": "https://app.example.org/notify",
  "backchannelAuthenticationRequestSigningAlg": "",
  "backchannelUserCodeParameterSupported": false
}
```

### Delivery Modes

The Client will receive the ID Token, Access Token, and optionally Refresh Token through either the `poll`, `ping`, or `push` modes. This choice MUST be 
established when the relying party is registered with CAS.

- Poll Mode

When configured in Poll mode, the Client will poll the token endpoint to get a response with the tokens. That is, the authentication result is retrieved by 
the Client by polling the CAS token endpoint using the CIBA grant type.

- Ping Mode

When configured in Ping mode, CAS will send a request to a callback URI previously registered by the Client 
with the unique identifier returned from the Backchannel Authentication Endpoint. Upon receipt of the notification, 
the Client makes a request to the token endpoint to obtain the tokens.

- Push Mode

When configured in Push mode, CAS will send a `POST` request with the tokens to a callback URI previously registered by the Client.


