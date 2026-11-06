---
layout: default
title: CAS - OpenID Connect - Client-Initiated Backchannel Authentication (CIBA)
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

### Signed Authentication Requests

An application may send its authentication request as a signed request object, as
[the specification](https://openid.net/specs/openid-client-initiated-backchannel-authentication-core-1_0.html#signed_auth_request)
allows. The request object is verified with the keys of the application that authenticated the request, whether or not
the request names it with `client_id`, and must be signed with an asymmetric algorithm. It must name the application as
its issuer, be addressed to CAS and carry `exp`, `iat`, `nbf` and `jti`; it may not be issued in the future, may be valid
for 60 minutes at most, and is used once: its `jti` is remembered until it expires. Request objects are refused
altogether when CAS is configured not to support the request parameter. A request object that does not meet
these rules is refused with `invalid_request`, rather than set aside for the other parameters of the request.

### Delivery Modes

The Client will receive the ID Token, Access Token, and optionally Refresh Token through either the `poll`, `ping`, or `push` modes. This choice MUST be 
established when the relying party is registered with CAS.

- Poll Mode

When configured in Poll mode, the Client will poll the token endpoint to get a response with the tokens. That is, the authentication result is retrieved by 
the Client by polling the CAS token endpoint using the CIBA grant type. Until the user approves the request, the token endpoint
answers with `authorization_pending`. An `auth_req_id` that has expired is refused with `expired_token`, and one that is unknown
or has already been redeemed is refused with `invalid_grant`. An expired request is only recognized as such while CAS still holds
it; once the ticket registry has removed it, it is reported as unknown.

- Ping Mode

When configured in Ping mode, CAS will send a request to a callback URI previously registered by the Client 
with the unique identifier returned from the Backchannel Authentication Endpoint. Upon receipt of the notification, 
the Client makes a request to the token endpoint to obtain the tokens.

- Push Mode

When configured in Push mode, CAS will send a `POST` request with the tokens to a callback URI previously registered by the Client.


