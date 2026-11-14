# REST Authentication

Source: https://apereo.github.io/cas/development/authentication/Rest-Authentication.html

> **Be Careful**
>
> This documentation describes how to delegate and submit authentication requests to a remote REST endpoint. It has nothing to do with the native CAS REST API, whose configuration and caveats are [documented here](https://apereo.github.io/cas/development/protocol/REST-Protocol.html).

REST authentication is enabled by including the following dependencies in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-rest-authentication"
}
```

This allows the CAS server to reach to a remote REST endpoint via a `POST` for verification of credentials. Credentials are passed via an `Authorization` header whose value is `Basic XYZ` where XYZ is a Base64 encoded version of the credentials.

The response that is returned must be accompanied by a `200` status code where the body should contain `id` and `attributes` fields, the latter being optional, which represent the authenticated principal for CAS:

```json
{
  "@class": "org.apereo.cas.authentication.principal.SimplePrincipal",
  "id": "casuser",
  "attributes": {
    "@class": "java.util.LinkedHashMap",
    "names": [
      "java.util.List", ["cas", "user"]
    ]
  }
}
```

Expected responses from the REST endpoint are mapped to CAS as such:

| Code | Result |
| --- | --- |
| `200` | Successful authentication. |
| `403` | Produces a `AccountDisabledException` |
| `404` | Produces a `AccountNotFoundException` |
| `423` | Produces a `AccountLockedException` |
| `412` | Produces a `AccountExpiredException` |
| `428` | Produces a `AccountPasswordMustChangeException` |
| Other | Produces a `FailedLoginException` |

## Authentication Warnings

The remote REST endpoint can send warnings back to the CAS server using custom headers. If the authentication is successful, these warnings will be shown to the user directly after the login.

| Header | Description |
| --- | --- |
| `X-CAS-Warning` | For each `X-CAS-Warning` header present in the response, a corresponding message will be shown to the user. The header value can either be the key for a [localized message](https://apereo.github.io/cas/development/ux/User-Interface-Customization-Localization.html) or the message itself. |
| `X-CAS-PasswordExpirationDat` | If this header is present in the response and contains a `RFC1123 date` a special message will be shown to warn the user about the expiring password. If a [password management provider](https://apereo.github.io/cas/development/password_management/Password-Management.html) is configured, the user will be able to directly change the password. |

## Configuration

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.rest[0].password-encoder.encoding-algorithm` (Required; type `String`): The encoding algorithm to use such as `MD5`. Relevant when the type used is `DEFAULT` or `GLIBC_CRYPT`. When used with `PasswordEncoderTypes#PBKDF2`, it should be one of `PBKDF2WithHmacSHA1`, `PBKDF2WithHmacSHA256` or `PBKDF2WithHmacSHA512`.
- `cas.authn.rest[0].password-encoder.type` (Required; type `String`; default `NONE`): Define the password encoder type to use. Type may be specified as blank or `NONE` to disable password encoding. It may also refer to a fully-qualified class name that implements the Spring Security's `PasswordEncoder` interface if you wish you define your own encoder. The following types may be used: - `NONE`: No password encoding (i.e. plain-text) takes place. - `DEFAULT`: Use the `DefaultPasswordEncoder` of CAS. For message-digest algorithms via `character-encoding` and `encoding-algorithm`. - `BCRYPT`: Use the `BCryptPasswordEncoder` based on the strength provided and an optional secret. - `SCRYPT`: Use the `SCryptPasswordEncoder`. - `PBKDF2`: Use the `Pbkdf2PasswordEncoder` based on the strength provided and an optional secret. - `STANDARD`: Use the `StandardPasswordEncoder` based on the secret provided. - `SSHA`: Use the `LdapShaPasswordEncoder` supports Ldap SHA and SSHA (salted-SHA). The values are base-64 encoded and have the label {SHA} or {SSHA} prepended to the encoded hash. - `GLIBC_CRYPT`: Use the `GlibcCryptPasswordEncoder` based on the `encoding-algorithm`, strength provided and an optional secret. - `org.example.MyEncoder`: An implementation of `PasswordEncoder` of your own choosing. - `file:///path/to/script.groovy`: Path to a Groovy script charged with handling password encoding operations.
- `cas.authn.rest[0].uri` (Required; type `String`): Endpoint URI to use for verification of credentials.
- `cas.authn.rest[0].method` (Optional; type `String`; default `POST`): HTTP method to use when contacting the REST API for authentication.
- `cas.authn.rest[0].name` (Optional; type `String`): Name of the authentication handler.
- `cas.authn.rest[0].order` (Optional; type `Integer`): Order of the authentication handler in the chain.
- `cas.authn.rest[0].password-encoder.character-encoding` (Optional; type `String`; default `UTF-8`): The encoding algorithm to use such as 'UTF-8'. Relevant when the type used is `DEFAULT`.
- `cas.authn.rest[0].password-encoder.hash-length` (Optional; type `int`; default `16`): When used by `PasswordEncoderTypes#ARGON2`, it indicates the hash strength/length.
- `cas.authn.rest[0].password-encoder.iterations` (Optional; type `int`; default `310000`): When used by `PasswordEncoderTypes#PBKDF2`, it indicates the required number of iterations.
- `cas.authn.rest[0].password-encoder.secret` (Optional; type `String`): Secret to use with `PasswordEncoderTypes#STANDARD`, `PasswordEncoderTypes#PBKDF2`, `PasswordEncoderTypes#BCRYPT`, `PasswordEncoderTypes#GLIBC_CRYPT` password encoders. Secret usually is an optional setting.
- `cas.authn.rest[0].password-encoder.strength` (Optional; type `int`; default `16`): Strength or number of iterations to use for password hashing. Usually relevant when dealing with `PasswordEncoderTypes#BCRYPT`, `PasswordEncoderTypes#PBKDF2` or `PasswordEncoderTypes#GLIBC_CRYPT`. When used by `PasswordEncoderTypes#ARGON2` or `PasswordEncoderTypes#PBKDF2`, it indicates the salt strength.
- `cas.authn.rest[0].state` (Optional; type `AuthenticationHandlerStates`; default `ACTIVE`): Define the scope and state of this authentication handler and the lifecycle in which it can be invoked or activated. Available values are as follows: - `ACTIVE`: Active authentication handler, and is invoked by default automatically to verify credentials globally. - `STANDBY`: Authentication handler is in a semi-enabled state, waiting to be called only on-demand when explicitly asked for.

**Password encoding**

If you need to design your own password encoding scheme where the type is specified as a fully qualified Java class name, the structure of the class would be similar to the following:

```java
package org.example.cas;

import org.springframework.security.crypto.codec.*;
import org.springframework.security.crypto.password.*;

public class MyEncoder extends AbstractPasswordEncoder {
    @Override
    protected byte[] encode(CharSequence rawPassword, byte[] salt) {
        return ...
    }
}
```

If you need to design your own password encoding scheme where the type is specified as a path to a Groovy script, the structure of the script would be similar to the following:

```groovy
import java.util.*

byte[] run(final Object... args) {
    def (rawPassword,generatedSalt,logger,applicationContext) = args
    logger.debug("Encoding password...")
    return ...
}

Boolean matches(final Object... args) {
    def (rawPassword,encodedPassword,logger,applicationContext) = args
    logger.debug("Does match or not ?");
    return ...
```

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development//integration/Apache-Groovy-Scripting.html).

## Multitenancy

Configuration settings for database authentication can be specified in a multitenant environment. Please [review this guide](https://apereo.github.io/cas/development/multitenancy/Multitenancy-Tenant-Properties.html) for more information.
