# OpenID Connect - JWT Authorization Grant

Source: https://apereo.github.io/cas/development/authentication/OIDC-Authentication-JWT-Bearer.html

The JWT Authorization grant (also known as JWT Bearer Token grant identified as `urn:ietf:params:oauth:client-assertion-type:jwt-bearer`) is a grant type that allows a client application to obtain an access token by presenting a JWT assertion to the CAS server, instead of using a username/password or client credentials. In the JWT Bearer flow, the client application creates and signs a JWT that asserts its identity and then exchanges that token for an access token by sending a `POST` request to the CAS token endpoint URL passing the JWT under the `assertion` parameter.

A few considerations:

1. The JWT **MUST** be signed. CAS must be able to verify the signature using the public key associated with the client.
2. The JWT **MUST** include claims such as `client_id`, `iss`, `sub`, `aud`, and `exp`.
3. The `aud` claim **MUST** match the token endpoint URL of the CAS server.
4. The client application **MUST** ensure the JWT is valid and has not expired.
5. CAS may optionally be configured to also create and share ID tokens and refresh tokens as part of the response.

## Configuration

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.oidc.id-token.collector-script.location` (Optional; type `Resource`)
- `cas.authn.oidc.id-token.generate-for-jwt-bearer-grant-type` (Optional; type `Boolean`; default `false`): Flag to indicate whether the JWT bearer grant type is supported for the purposes of requesting ID tokens. Setting this to true will force to generate ID tokens when the JWT bearer grant type is used.
- `cas.authn.oidc.id-token.include-id-token-claims` (Optional; type `Boolean`; default `true`): As per OpenID Connect Core section 5.4, "The Claims requested by the `profile`, `email`, `address`, and `phone` scope values are returned from the userinfo endpoint", except for `response_type`=`id_token`, where they are returned in the id_token (as there is no access token issued that could be used to access the userinfo endpoint). The Claims requested by the profile, email, address, and phone scope values are returned from the userinfo endpoint when a `response_type` value is used that results in an access token being issued. However, when no access token is issued (which is the case for the `response_type` value `id_token`), the resulting Claims are returned in the ID Token. Setting this flag to true will force CAS to include claims in the ID token regardless of the response type. Note that this setting **MUST ONLY** be used as a last resort, to stay compliant with the specification as much as possible. ** DO NOT** use this setting without due consideration. Note that this setting is set to `true` by default mainly provided to preserve backward compatibility with previous CAS versions that included claims into the ID token without considering the response type. The behavior of this setting may change and it may be removed in future CAS releases.
- `cas.authn.oidc.id-token.max-time-to-live-in-seconds` (Optional; type `String`; default `PT8H`): Hard timeout to kill the ID token and expire it.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.

## Applications

Applications that wish to take advantage of this capability can be registered with CAS as such:

```json
{
  "@class": "org.apereo.cas.services.OidcRegisteredService",
  "clientId": "client-id",
  "clientSecret": "secret",
  "serviceId": "^https://app.example.org/oidc",
  "name": "MyApplication",
  "id": 1,
  "generateRefreshToken": true,
  "jwks": "file:/path/to/application-keystore.jwks",
  "supportedGrantTypes": [ "java.util.HashSet", [ "urn:ietf:params:oauth:client-assertion-type:jwt-bearer" ] ]
}
```

Note that JWT validation operations may also be performed via public keys that are [dynamically registered](https://apereo.github.io/cas/development/authentication/OIDC-Authentication-JWKS-Clients.html) with CAS. In doing so, it is recommended that the provided JWT here contains a `kid` field that points to the registered public key that should be used for validation. Alternatively, the JWT may have a `jwk` header whose thumbprint matches the registered public key. Without the appropriate `kid` or `jwk` CAS may not be able to determine which public key to use for validation.

Please [see this guide](https://apereo.github.io/cas/development/authentication/OIDC-Authentication-JWKS-Clients.html) for more info.
