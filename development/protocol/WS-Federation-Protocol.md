# WS Federation Protocol

Source: https://apereo.github.io/cas/development/protocol/WS-Federation-Protocol.html

CAS can act as a standalone identity provider, presenting support for the WS-Federation Passive Requester Profile. The core functionality is built on top of [Apache Fediz](http://cxf.apache.org/fediz.html) whose architecture is described [here](http://cxf.apache.org/fediz-architecture.html).

> **Remember**
>
> The functionality described here allows CAS to act as an identity provider to support the WS-Federation protocol. If you wish to do the opposite and hand off authentication to an external identity provider that supports WS-Federation, you may take advantage of [Delegation](https://apereo.github.io/cas/development/integration/ADFS-Integration.html) as one integration option.

## Security Token Service

The WS-Trust OASIS standard specifies a runtime component called Security Token Service. A service consumer requests a security token from the STS which is sent to the service provider. Either the service provider can validate the security token on its own or sends a request to the STS for validation. This pattern is based on an indirect trust relationship between the service provider and the STS instead of a direct trust between the service provider and service consumer. As long as the service consumer is in the possession of a security token issued by a trusted STS, the service provider accepts this security token.

A key benefit of the STS is the reduced complexity for applications. A web service consumer does not have to know how to create the various types of security tokens its service providers require. Instead, it sends a request to the STS containing the requirements of the client and the service provider and attaches the returned security token to the outgoing SOAP message to the service provider.

Support is enabled by including the following dependency in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
        Including this module in the CAS WAR overlay is optional and unnecessary. This module is automatically included
        and bundled with the CAS server distribution and there are alternative options baked in to allow one to replace
        this module with another. The entry below is listed for reference only.
    */
    implementation "org.apereo.cas:cas-server-support-ws-sts"
}
```

> **YAGNI**
>
> You do not need to explicitly include this component in your configuration and overlays. This is just to teach you that it exists. The security token service will be pulled in automatically once you declare the identity provider. Only include this module in your overlay if you need compile-time access to the components within.

### Endpoints

| Endpoint | Description |
| --- | --- |
| `/ws/sts` | Presents the list of available SOAP services and their WSDL configuration for each REALM defined in the configuration. |

### Security Tokens

Security tokens issued are treated as CAS tickets, stored in the ticket registry under the prefix `STS` and follow the same semantics as all other ticket types when it comes to persistence, replication, etc. These tokens are closely tied to the lifetime of the ticket-granting tickets and match their expiration policy. Tokens themselves do not have a lifespan outside a valid ticket-granting ticket and support for ticket lifetime configuration is not present.

## WS Federation Identity Provider

The security model of the STS builds on the foundation established by WS-Security and WS-Trust. The primary issue for Web browsers is that there is no easy way to directly send web service (SOAP) requests. Consequently, the processing must be performed within the confines of the base HTTP 1.1 functionality (`GET`, `POST`, redirects, and cookies) and conform as closely as possible to the WS-Trust protocols for token acquisition. The IdP is in charge of transforming the sign-in request of the browser to a SOAP request for the STS and the response of the STS to the sign-in response for the browser. Further the browser user must authenticate with the IdP.

Support is enabled by including the following dependency in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-ws-idp"
}
```

### Endpoints

| Endpoint | Description |
| --- | --- |
| `/ws/idp/metadata` | Displays the current federation metadata based on the configuration realm for the identity provider. |
| `/ws/idp/federation` | Endpoint to receive initial `GET` authentication requests from clients, typically identified as the `issuer`. |

## Realms

At this point, by default security token service’s endpoint operate using a single realm configuration and identity provider configuration is only able to recognize and request tokens for a single realm. While support for multiple realms is not there yet, in general the underlying configuration should allow for that feature to exist in later releases. The default realm recognized by CAS is set to be `urn:org:apereo:cas:ws:idp:realm-CAS`. Registration of clients need to ensure this value is matched.

## Clients

Please see [this guide](https://apereo.github.io/cas/development/protocol/WS-Federation-Protocol-Clients.html).

## Claims

Please see [this guide](https://apereo.github.io/cas/development/protocol/WS-Federation-Protocol-Claims.html).

## Token Types

The following token types are supported by CAS:

| Type |
| --- |
| `http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV1.1` |
| `http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV2.0` |
| `urn:ietf:params:oauth:token-type:jwt` |
| `http://docs.oasis-open.org/ws-sx/ws-secureconversation/200512/sct` |

Token type may be configured on a per-service basis:

```json
{
  "@class" : "org.apereo.cas.ws.idp.services.WSFederationRegisteredService",
  "serviceId" : "https://wsfed.example.org/.+",
  "realm" : "urn:wsfed:example:org:sampleapplication",
  "name" : "WSFED",
  "id" : 1,
  "tokenType": "http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV1.1"
}
```

## Configuration

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.wsfed-idp.idp.realm` (Required; type `String`; default `urn:org:apereo:cas:ws:idp:realm-CAS`): At this point, by default security token service’s endpoint operate using a single realm configuration and identity provider configuration is only able to recognize and request tokens for a single realm. Registration of clients need to ensure this value is matched.
- `cas.authn.wsfed-idp.idp.realm-name` (Required; type `String`; default `CAS`): Realm name.
- `cas.authn.wsfed-idp.sts.crypto.encryption.key` (Required; type `String`): The encryption key is a string whose length is defined by the encryption key size setting.
- `cas.authn.wsfed-idp.sts.crypto.signing.key` (Required; type `String`): The signing key is a string whose length is defined by the signing key size setting.
- `cas.authn.wsfed-idp.sts.realm.issuer` (Required; type `String`; default `CAS`): Issuer/name of the realm identified and registered with STS.
- `cas.authn.wsfed-idp.sts.realm.key-password` (Required; type `String`): Key alias associated with the this realm.
- `cas.authn.wsfed-idp.sts.realm.keystore-file` (Required; type `String`): Keystore path associated with the this realm.
- `cas.authn.wsfed-idp.sts.realm.keystore-password` (Required; type `String`): Keystore password associated with the this realm.
- `cas.authn.wsfed-idp.sts.conditions-accept-client-lifetime` (Optional; type `Boolean`; default `true`): Set whether client lifetime is accepted.
- `cas.authn.wsfed-idp.sts.conditions-fail-lifetime-exceedance` (Optional; type `Boolean`; default `false`): If requested lifetime exceeds shall it fail (default) or overwrite with maximum lifetime.
- `cas.authn.wsfed-idp.sts.conditions-future-time-to-live` (Optional; type `String`; default `PT60S`): Get how long (in seconds) a client-supplied Created Element is allowed to be in the future. The default is 60 seconds to avoid common problems relating to clock skew.
- `cas.authn.wsfed-idp.sts.conditions-lifetime` (Optional; type `String`; default `PT30M`): Set the default lifetime in seconds for issued SAML tokens.
- `cas.authn.wsfed-idp.sts.conditions-max-lifetime` (Optional; type `String`; default `PT12H`): Set the maximum lifetime in seconds for issued SAML tokens.
- `cas.authn.wsfed-idp.sts.crypto.alg` (Optional; type `String`): The signing/encryption algorithm to use.
- `cas.authn.wsfed-idp.sts.crypto.enabled` (Optional; type `Boolean`; default `true`): Whether crypto operations are enabled.
- `cas.authn.wsfed-idp.sts.crypto.encryption.key-size` (Optional; type `Integer`; default `512`): The encryption key size.
- `cas.authn.wsfed-idp.sts.crypto.signing.key-size` (Optional; type `Integer`; default `512`): The signing key size.
- `cas.authn.wsfed-idp.sts.crypto.strategy-type` (Optional; type `String`; default `ENCRYPT_AND_SIGN`): Control the cipher sequence of operations. The accepted values are: - `ENCRYPT_AND_SIGN`: Encrypt the value first, and then sign. - `SIGN_AND_ENCRYPT`: Sign the value first, and then encrypt.
- `cas.authn.wsfed-idp.sts.custom-claims` (Optional; type `List<String>`): Collection of fully-qualified claims prefixed with the appropriate namespace that are expected to be released via attribute release policy.
- `cas.authn.wsfed-idp.sts.encrypt-tokens` (Optional; type `Boolean`; default `true`): Whether tokens generated by STS should encrypted.
- `cas.authn.wsfed-idp.sts.encryption-keystore-file` (Optional; type `String`): Keystore path used to encrypt tokens.
- `cas.authn.wsfed-idp.sts.encryption-keystore-password` (Optional; type `String`): Keystore password used to encrypt tokens.
- `cas.authn.wsfed-idp.sts.realm.keystore-alias` (Optional; type `String`): Key alias associated with the this realm.
- `cas.authn.wsfed-idp.sts.sign-tokens` (Optional; type `Boolean`; default `true`): Set whether the provided token will be signed or not. Default is true.
- `cas.authn.wsfed-idp.sts.signing-keystore-file` (Optional; type `String`): Keystore path used to sign tokens.
- `cas.authn.wsfed-idp.sts.signing-keystore-password` (Optional; type `String`): Keystore password used to sign tokens.
- `cas.authn.wsfed-idp.sts.subject-name-id-format` (Optional; type `String`; default `unspecified`): When generating a SAML token, indicates the subject name-id format to use. Accepted values are: - unspecified - email - transient - persistent - entity
- `cas.authn.wsfed-idp.sts.subject-name-qualifier` (Optional; type `String`; default `http://cxf.apache.org/sts`): When generating a SAML token, indicates the subject name-id qualifier to use.

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

You may also need to declare the following repository in your CAS Overlay to be able to resolve dependencies:

```groovy
repositories {
    maven { 
        mavenContent { releasesOnly() }
        url "https://build.shibboleth.net/maven/releases/" 
    }
}
```

## Troubleshooting

To enable additional logging, modify the logging configuration file to add the following:

```xml
<Logger name="org.apache.cxf" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>
```
