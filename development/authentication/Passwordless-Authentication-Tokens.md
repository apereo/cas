# Passwordless Authentication - Token Management

Source: https://apereo.github.io/cas/development/authentication/Passwordless-Authentication-Tokens.html

The following strategies define how issued tokens may be managed by CAS.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.passwordless.tokens.crypto.encryption.key` (Required; type `String`): The encryption key is a string whose length is defined by the encryption key size setting.
- `cas.authn.passwordless.tokens.crypto.signing.key` (Required; type `String`): The signing key is a string whose length is defined by the signing key size setting.
- `cas.authn.passwordless.tokens.core.expiration` (Optional; type `String`; default `PT180S`): Indicate how long should the token be considered valid.
- `cas.authn.passwordless.tokens.crypto.alg` (Optional; type `String`): The signing/encryption algorithm to use.
- `cas.authn.passwordless.tokens.crypto.enabled` (Optional; type `Boolean`; default `true`): Whether crypto operations are enabled.
- `cas.authn.passwordless.tokens.crypto.encryption.key-size` (Optional; type `Integer`; default `512`): The encryption key size.
- `cas.authn.passwordless.tokens.crypto.signing.key-size` (Optional; type `Integer`; default `512`): The signing key size.
- `cas.authn.passwordless.tokens.crypto.strategy-type` (Optional; type `String`; default `ENCRYPT_AND_SIGN`): Control the cipher sequence of operations. The accepted values are: - `ENCRYPT_AND_SIGN`: Encrypt the value first, and then sign. - `SIGN_AND_ENCRYPT`: Sign the value first, and then encrypt.

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

| Option | Description |
| --- | --- |
| Memory | This is the default option where tokens are kept in memory using a cache with a configurable expiration period. |
| MongoDb | Please [see this guide](https://apereo.github.io/cas/development/authentication/Passwordless-Authentication-Tokens-MongoDb.html). |
| JPA | Please [see this guide](https://apereo.github.io/cas/development/authentication/Passwordless-Authentication-Tokens-JPA.html). |
| REST | Please [see this guide](https://apereo.github.io/cas/development/authentication/Passwordless-Authentication-Tokens-Rest.html). |
| Custom | Please [see this guide](https://apereo.github.io/cas/development/authentication/Passwordless-Authentication-Tokens-Custom.html). |
