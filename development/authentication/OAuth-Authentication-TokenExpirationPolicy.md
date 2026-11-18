# Token Expiration Policy - OAuth Authentication

Source: https://apereo.github.io/cas/development/authentication/OAuth-Authentication-TokenExpirationPolicy.html

The expiration policy for OAuth tokens is controlled by CAS settings and properties. Note that while access and refresh tokens may have their own lifetime and expiration policy, they are typically upper-bound to the length of the CAS single sign-on session.

## OAuth Codes

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.oauth.code.number-of-uses` (Optional; type `Integer`; default `1`): Number of times this code is valid and can be used.
- `cas.authn.oauth.code.remove-related-access-tokens` (Optional; type `Boolean`; default `false`): Revoke the access and refresh tokens issued from an authorization code when the code is presented again after it was redeemed, as RFC 6749 recommends. When enabled, CAS keeps a short-lived record of the tokens issued from each code it redeems, for as long as transient session tickets live, and revokes them when the code is presented again.
- `cas.authn.oauth.code.storage-name` (Optional; type `String`; default `oauthCodesCache`): The storage object name used and created by CAS to hold OAuth codes in the backing ticket registry implementation.
- `cas.authn.oauth.code.time-to-kill-in-seconds` (Optional; type `Long`; default `30`): Duration in seconds where the code is valid.

## OAuth Access Tokens

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.oauth.access-token.crypto.encryption.key` (Required; type `String`): The encryption key is a string whose length is defined by the encryption key size setting.
- `cas.authn.oauth.access-token.crypto.signing.key` (Required; type `String`): The signing key is a string whose length is defined by the signing key size setting.
- `cas.authn.oauth.access-token.create-as-jwt` (Optional; type `Boolean`; default `false`): Create access token as JWTs.
- `cas.authn.oauth.access-token.crypto.alg` (Optional; type `String`): The signing/encryption algorithm to use.
- `cas.authn.oauth.access-token.crypto.enabled` (Optional; type `Boolean`; default `true`): Whether crypto operations are enabled.
- `cas.authn.oauth.access-token.crypto.encryption-enabled` (Optional; type `Boolean`; default `true`): Whether crypto encryption operations are enabled.
- `cas.authn.oauth.access-token.crypto.encryption.key-size` (Optional; type `Integer`; default `512`): The encryption key size.
- `cas.authn.oauth.access-token.crypto.signing-enabled` (Optional; type `Boolean`; default `true`): Whether crypto signing operations are enabled.
- `cas.authn.oauth.access-token.crypto.signing.key-size` (Optional; type `Integer`; default `512`): The signing key size.
- `cas.authn.oauth.access-token.crypto.strategy-type` (Optional; type `String`; default `ENCRYPT_AND_SIGN`): Control the cipher sequence of operations. The accepted values are: - `ENCRYPT_AND_SIGN`: Encrypt the value first, and then sign. - `SIGN_AND_ENCRYPT`: Sign the value first, and then encrypt.
- `cas.authn.oauth.access-token.include-claims-in-jwt` (Optional; type `Boolean`; default `true`): Whether CAS should include extra CAS attributes as claims in the JWT access token. This setting is only relevant if the access token is a determined to be a JWT.
- `cas.authn.oauth.access-token.max-active-tokens-allowed` (Optional; type `Long`; default `0`): Maximum number of active access tokens that an application can receive. If the application requests more that this limit, the request will be denied and the access token will not be issued.
- `cas.authn.oauth.access-token.max-time-to-live-in-seconds` (Optional; type `String`; default `PT8H`): Hard timeout to kill the access token and expire it.
- `cas.authn.oauth.access-token.storage-name` (Optional; type `String`; default `oauthAccessTokensCache`): The storage object name used and created by CAS to hold OAuth access tokens in the backing ticket registry implementation.
- `cas.authn.oauth.access-token.time-to-kill-in-seconds` (Optional; type `String`; default `PT2H`): Sliding window for the access token expiration policy. Essentially, this is an idle time out.

**Signing & encryption**

This CAS feature is able to accept signing and encryption crypto keys. In most scenarios if keys are not provided, CAS will auto-generate them. The following instructions apply if you wish to manually and beforehand create the signing and encryption keys.

Note that if you are asked to create a [JWK](https://tools.ietf.org/html/rfc7517) of a certain size for the key, you are to use the following set of commands to generate the token:

```bash
wget https://raw.githubusercontent.com/apereo/cas/master/etc/jwk-gen.jar
java -jar jwk-gen.jar -t oct -s [size]
```

The outcome would be similar to:

```json
{
  "kty": "oct",
  "kid": "...",
  "k": "..."
}
```

The generated value for `k` needs to be assigned to the relevant CAS settings. Note that keys generated via the above algorithm are processed by CAS using the Advanced Encryption Standard (`AES`) algorithm which is a specification for the encryption of electronic data established by the U.S. National Institute of Standards and Technology.

---

## OAuth Device Tokens

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.oauth.device-token.max-time-to-live-in-seconds` (Optional; type `String`; default `PT5M`): Hard timeout to kill the device token and expire it.
- `cas.authn.oauth.device-token.refresh-interval` (Optional; type `String`; default `PT15S`): The device refresh interval. The client should attempt to acquire an access token every few seconds (at a rate specified by interval) by POSTing to the access token endpoint on the server.
- `cas.authn.oauth.device-token.storage-name` (Optional; type `String`; default `oauthDeviceTokensCache`): The storage object name used and created by CAS to hold OAuth device tokens in the backing ticket registry implementation.

## OAuth Refresh Tokens

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.oauth.refresh-token.create-as-jwt` (Optional; type `Boolean`; default `false`): Create access token as JWTs.
- `cas.authn.oauth.refresh-token.max-active-tokens-allowed` (Optional; type `Long`; default `0`): Maximum number of active refresh tokens that an application can receive. If the application requests more that this limit, the request will be denied and the access token will not be issued.
- `cas.authn.oauth.refresh-token.storage-name` (Optional; type `String`; default `oauthRefreshTokensCache`): The storage object name used and created by CAS to hold OAuth refresh tokens in the backing ticket registry implementation.
- `cas.authn.oauth.refresh-token.time-to-kill-in-seconds` (Optional; type `String`; default `P14D`): Hard timeout beyond which the refresh token is considered expired.
- `cas.authn.oauth.refresh-token.track-access-tokens` (Optional; type `Boolean`; default `true`): If true, the refresh token will keep track of the access tokens issued using it. The access tokens will remain attached to the refresh token as long as the refresh token is valid and they are not periodically on on-demand checked for validity or removed from the refresh token if they expire. The tracking mechanism is only for history and auditing purposes.

## OAuth Device User Codes

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.oauth.device-user-code.max-time-to-live-in-seconds` (Optional; type `String`; default `PT1M`): Hard timeout to kill the token and expire it.
- `cas.authn.oauth.device-user-code.storage-name` (Optional; type `String`; default `oauthDeviceUserCodesCache`): The storage object name used and created by CAS to hold OAuth device user codes in the backing ticket registry implementation.
- `cas.authn.oauth.device-user-code.user-code-length` (Optional; type `Integer`; default `8`): Length of the generated user code.

## Per Service

The expiration policy of certain OAuth tokens can be conditionally decided on a per-application basis. The candidate service whose token expiration policy is to deviate from the default configuration must be designed as the following snippets demonstrate.

**OAuth Code**

The expiration policy of codes can be defined on a per application basis:

```json
{
  "@class" : "org.apereo.cas.support.oauth.services.OAuthRegisteredService",
  "clientId": "clientid",
  "clientSecret": "clientSecret",
  "serviceId" : "^(https|imaps)://<redirect-uri>.*",
  "name" : "OAuthService",
  "id" : 100,
  "codeExpirationPolicy": {
    "@class": "org.apereo.cas.support.oauth.services.DefaultRegisteredServiceOAuthCodeExpirationPolicy",
    "numberOfUses": 1,
    "timeToLive": "PT10S"
  }
}
```

**OAuth Access Tokens**

The expiration policy of access tokens can be defined on a per application basis:

```json
{
  "@class" : "org.apereo.cas.support.oauth.services.OAuthRegisteredService",
  "clientId": "clientid",
  "clientSecret": "clientSecret",
  "serviceId" : "^(https|imaps)://<redirect-uri>.*",
  "name" : "OAuthService",
  "id" : 100,
  "accessTokenExpirationPolicy": {
    "@class": "org.apereo.cas.support.oauth.services.DefaultRegisteredServiceOAuthAccessTokenExpirationPolicy",
    "maxTimeToLive": "PT1000S",
    "timeToKill": "PT100S",
    "maxActiveTokens": 0
  }
}
```

**OAuth Device Tokens**

The expiration policy of device tokens can be defined on a per application basis:

```json
{
  "@class" : "org.apereo.cas.support.oauth.services.OAuthRegisteredService",
  "clientId": "clientid",
  "clientSecret": "clientSecret",
  "serviceId" : "^(https|imaps)://<redirect-uri>.*",
  "name" : "OAuthService",
  "id" : 100,
  "deviceTokenExpirationPolicy": {
    "@class": "org.apereo.cas.support.oauth.services.DefaultRegisteredServiceOAuthDeviceTokenExpirationPolicy",
    "timeToKill": "PT100S"
  }
}
```

**OAuth Refresh Tokens**

The expiration policy of refresh tokens can be defined on a per application basis:

```json
{
  "@class" : "org.apereo.cas.support.oauth.services.OAuthRegisteredService",
  "clientId": "clientid",
  "clientSecret": "clientSecret",
  "serviceId" : "^(https|imaps)://<redirect-uri>.*",
  "name" : "OAuthService",
  "id" : 100,
  "refreshTokenExpirationPolicy": {
    "@class": "org.apereo.cas.support.oauth.services.DefaultRegisteredServiceOAuthRefreshTokenExpirationPolicy",
    "timeToKill": "PT100S",
    "maxActiveTokens": 0
  }
}
```
