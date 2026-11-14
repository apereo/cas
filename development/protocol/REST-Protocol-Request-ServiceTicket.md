# Service Ticket - REST Protocol

Source: https://apereo.github.io/cas/development/protocol/REST-Protocol-Request-ServiceTicket.html

The below snippets show one might request a service ticket using the semantics of the CAS protocol:

```bash
POST /cas/v1/tickets/{TGT id} HTTP/1.0

service={form encoded parameter for the service url}
```

You may also specify a `renew` parameter to obtain a service ticket that can be accepted by a service that only wants tickets issued from the presentation of the user’s primary credentials. In that case, user credentials have to be passed in the request, for example, as `username` and `password` parameters.

```bash
POST /cas/v1/tickets/{TGT id} HTTP/1.0

service={form encoded parameter for the service url}&renew=true&username=casuser&password=password
```

You may also submit service ticket requests using the semantics [SAML1 protocol](https://apereo.github.io/cas/development/protocol/SAML-v1-Protocol.html).

## Successful Response

```bash
200 OK
ST-1-FFDFHDSJKHSDFJKSDHFJKRUEYREWUIFSD2132
```

## JWT Service Tickets

Service tickets created by the REST protocol may be issued as JWTs instead. See [this guide](https://apereo.github.io/cas/development/installation/Configure-ServiceTicket-JWT.html) to learn more.

Support is enabled by including the following in your overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-rest-tokens"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.token.crypto.encryption.key` (Required; type `String`): The encryption key is a string whose length is defined by the encryption key size setting.
- `cas.authn.token.crypto.signing.key` (Required; type `String`): The signing key is a string whose length is defined by the signing key size setting.
- `cas.authn.token.credential-criteria` (Optional; type `String`): A number of authentication handlers are allowed to determine whether they can operate on the provided credential and as such lend themselves to be tried and tested during the authentication handler selection phase. The credential criteria may be one of the following options: - 1) A regular expression pattern that is tested against the credential identifier. - 2) A fully qualified class name of your own design that implements `Predicate`. - 3) Path to an external Groovy script that implements the same interface.
- `cas.authn.token.crypto.alg` (Optional; type `String`): The signing/encryption algorithm to use.
- `cas.authn.token.crypto.enabled` (Optional; type `Boolean`; default `true`): Whether crypto operations are enabled.
- `cas.authn.token.crypto.encryption-enabled` (Optional; type `Boolean`; default `true`): Whether crypto encryption operations are enabled.
- `cas.authn.token.crypto.encryption.key-size` (Optional; type `Integer`; default `512`): The encryption key size.
- `cas.authn.token.crypto.signing-enabled` (Optional; type `Boolean`; default `true`): Whether crypto signing operations are enabled.
- `cas.authn.token.crypto.signing.key-size` (Optional; type `Integer`; default `512`): The signing key size.
- `cas.authn.token.crypto.strategy-type` (Optional; type `String`; default `ENCRYPT_AND_SIGN`): Control the cipher sequence of operations. The accepted values are: - `ENCRYPT_AND_SIGN`: Encrypt the value first, and then sign. - `SIGN_AND_ENCRYPT`: Sign the value first, and then encrypt.
- `cas.authn.token.name` (Optional; type `String`): Name of the authentication handler.
- `cas.authn.token.order` (Optional; type `Integer`): Order of the authentication handler in the chain.
- `cas.authn.token.sso-token-enabled` (Optional; type `Boolean`; default `false`): When set to true, will generate a token and store it as an authentication attribute into the single sign-on session. This token can be used later on for token-based authentication attempts, and should allow SSO access to the server.
- `cas.authn.token.state` (Optional; type `AuthenticationHandlerStates`; default `active`): Define the scope and state of this authentication handler and the lifecycle in which it can be invoked or activated. Available values are as follows: - `ACTIVE`: Active authentication handler, and is invoked by default automatically to verify credentials globally. - `STANDBY`: Authentication handler is in a semi-enabled state, waiting to be called only on-demand when explicitly asked for.
- `cas.authn.token.webflow.enabled` (Optional; type `Boolean`; default `true`): Whether webflow auto-configuration should be enabled.
- `cas.authn.token.webflow.order` (Optional; type `Integer`; default `0`): The order in which the webflow is configured.

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

**Authentication**

Certain authentication handlers are allowed to determine whether they can operate on the provided credential and as such lend themselves to be tried and tested during the authentication handler selection phase. The credential criteria may be one of the following options:

- A regular expression pattern that is tested against the credential identifier.
- A fully qualified class name of your own design that looks similar to the below example:

```java
import java.util.function.Predicate;
import org.apereo.cas.authentication.Credential;

public class PredicateExample implements Predicate<Credential> {
    @Override
    public boolean test(final Credential credential) {
        // Examine the credential and return true/false
    }
}
```

- Path to an external Groovy script that looks similar to the below example:

```groovy
import org.apereo.cas.authentication.Credential
import java.util.function.Predicate

class PredicateExample implements Predicate<Credential> {
    @Override
    boolean test(final Credential credential) {
        // test and return result
    }
}
```

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development//integration/Apache-Groovy-Scripting.html).
