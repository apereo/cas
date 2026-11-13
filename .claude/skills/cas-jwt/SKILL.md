---
name: cas-jwt
description: "Issue and read JWTs anywhere in CAS (JwtBuilder, JsonWebTokenEncryptor, OAuth/OIDC ID, access and refresh tokens, request objects, token exchange, token authentication, risk/QR tokens, the shell): CAS never issues or accepts unsigned (alg none) JWTs, per-service signing and encryption decisions, and how tests sign. Use when touching JWT creation, parsing, signing or encryption, or OIDC signing algorithms."
---

# JWTs in CAS

Discovery metadata and keystore algorithms are in `cas-oauth-oidc`.

## Unsigned JWTs

- CAS neither issues nor accepts unsigned JWTs (`alg: none`). Reading: `JwtBuilder.unpack` refuses a `PlainJWT`. Issuing:
  `JwtBuilder.build` throws when neither the service nor the default cipher can sign or encrypt, and
  `BaseTokenSigningAndEncryptionService.signTokenIfNecessary` throws when `shouldSignToken` is false. There is no
  `buildPlain`; tests that need an unsigned JWT build one with `new PlainJWT(claims).serialize()`.
- `shouldSignToken`/`shouldEncryptToken` per service: ID tokens always sign (`none` throws; there is no `signIdToken` property, and `OidcRegisteredService` ignores it in older definitions);
  user info signs when an algorithm is set or encryption is asked for (renderer returns plain JSON otherwise);
  introspection JWTs always sign (default `RS512`, also for `none`); UMA RPTs always sign with the UMA JWKS. `none` as an
  encryption algorithm means no encryption. Discovery filters `none` out of every signing algorithm list it publishes.
- `JsonWebTokenEncryptor` always sets `cty: JWT`, so its payload must be a signed JWT, never bare claims.
- The shell's `generate-jwt` and `generate-full-jwt` always sign. Their tests assert refusals by calling the command
  method directly, as `GenerateJwtCommandTests` already did for a blank subject, not through `runShellCommand`.
- Unsigned JWTs (`alg: none`): `JwtBuilder.unpack` returns null for them (`JwtBuilder.isUnsigned`), so request objects
  (encrypted-but-unsigned too), token exchange subject/actor JWTs, risk/QR tokens and JWT access/refresh tokens are
  refused; `OAuth20JwtAccessTokenEncodableCipher.encode` refuses to issue an unsigned JWT token (IllegalStateException);
  discovery never advertises `none` for request objects. Tests sign request objects with the client's keys or
  `accessTokenJwtBuilder.getDefaultTokenCipherExecutor().encode(claims.toString())`, never `PlainJWT`.
