# FIDO2 WebAuthn (Passkey) Multifactor Authentication

Source: https://apereo.github.io/cas/development/mfa/FIDO2-WebAuthn-Authentication.html

[WebAuthn](https://webauthn.io/) is an API that makes it very easy for a relying party, such as a web service, to integrate strong authentication into applications using support built in to all leading browsers and platforms. This means that web services can now easily offer their users strong authentication with a choice of authenticators such as security keys or built-in platform authenticators such as biometric readers.

> **Usage Warning!**
>
> To use WebAuthn support in a cluster, you must either enable session affinity (so that the same user always connects to the same node), or [replicate the web session](https://apereo.github.io/cas/development/webflow/Webflow-Customization-Sessions-ServerSide.html) across all nodes in the cluster.

Support is enabled by including the following module in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-webauthn"
}
```

> **WebAuthn vs Passkeys**
>
> **WebAuthn (Web Authentication)** is a W3C specification and browser API that enables web applications to register and authenticate users using public-key cryptography in a phishing-resistant way. ** Passkeys** are a specific type of WebAuthn credential designed to replace passwords by using asymmetric key pairs. During registration, an authenticator on the user’s device generates a private-public key pair; the public key is sent to the service, and the private key remains securely on the device (or synced via a cloud backup). In summary, WebAuthn is the underlying protocol/API that supports multiple authentication methods (hardware keys, platform authenticators, etc.), whereas passkeys are a user-facing credential format specifically built on WebAuthn for passwordless login.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.mfa.web-authn.core.application-id` (Required; type `String`): The extension input to set for the `appid` extension when initiating authentication operations. If this member is set, starting an assertion op will automatically set the `appid` extension input, and finish assertion op will adjust its verification logic to also accept this AppID as an alternative to the RP ID. By default, this is not set.
- `cas.authn.mfa.web-authn.core.relying-party-id` (Required; type `String`): The id that will be set as the rp parameter when initiating registration operations, and which id hash will be compared against. This is a required parameter. A successful registration or authentication operation requires rp id hash to exactly equal the SHA-256 hash of this id member. Alternatively, it may instead equal the SHA-256 hash of application id if the latter is present.
- `cas.authn.mfa.web-authn.core.relying-party-name` (Required; type `String`): The human-palatable name of the Relaying Party.
- `cas.authn.mfa.web-authn.crypto.encryption.key` (Required; type `String`): The encryption key is a string whose length is defined by the encryption key size setting.
- `cas.authn.mfa.web-authn.crypto.signing.key` (Required; type `String`): The signing key is a string whose length is defined by the signing key size setting.
- `cas.authn.mfa.web-authn.core.allow-primary-authentication` (Optional; type `Boolean`; default `false`): Configure the authentication flow to allow web-authn to be used as the first primary factor for authentication. Registered accounts with a valid webauthn registration record can choose to login using their device as the first step.
- `cas.authn.mfa.web-authn.core.allow-untrusted-attestation` (Optional; type `Boolean`; default `false`): If false finish registration op will only allow registrations where the attestation signature can be linked to a trusted attestation root. This excludes self attestation and none attestation. Regardless of the value of this option, invalid attestation statements of supported formats will always be rejected. For example, a "packed" attestation statement with an invalid signature will be rejected even if this option is set to true.
- `cas.authn.mfa.web-authn.core.allowed-origins` (Optional; type `String`): The allowed origins that returned authenticator responses will be compared against. The default is set to the server name. A successful registration or authentication operation requires origins to exactly equal one of these values.
- `cas.authn.mfa.web-authn.core.attestation-conveyance-preference` (Optional; type `String`; default `DIRECT`): Accepted values are: `DIRECT`, `INDIRECT`, `NONE`. The argument for the attestation parameter in registration operations. Unless your application has a concrete policy for authenticator attestation, it is recommended to leave this parameter undefined.
- `cas.authn.mfa.web-authn.core.authenticator-attachment` (Optional; type `String`): Specifies the desired authenticator attachment modality during credential registration. This is used by the WebAuthn client/browser to select which type of authenticator may be used to create the credential. - `PLATFORM`: an authenticator built into, or tightly bound to, the user's device, such as Touch ID, Windows Hello, Android/iOS passkeys. - `CROSS_PLATFORM`: an external/roaming authenticator usable across multiple devices, such as a USB, NFC, or BLE security key like a YubiKey. If unset, the client may use any available authenticator.
- `cas.authn.mfa.web-authn.core.display-name-attribute` (Optional; type `String`; default `displayName`): Name of the principal attribute that indicates the principal's display name, primarily used for device registration.
- `cas.authn.mfa.web-authn.core.enabled` (Optional; type `Boolean`; default `true`): Whether WebAuthn functionality should be activated and enabled.
- `cas.authn.mfa.web-authn.core.expire-devices` (Optional; type `Long`; default `30`): Expire and forget device registration records after this period.
- `cas.authn.mfa.web-authn.core.expire-devices-time-unit` (Optional; type `TimeUnit`; default `days`): Device registration record expiration time unit.
- `cas.authn.mfa.web-authn.core.hints` (Optional; type `List<String>`)
- `cas.authn.mfa.web-authn.core.multiple-device-registration-enabled` (Optional; type `Boolean`; default `false`): When enabled, allows the user/system to accept multiple accounts and device registrations per user, allowing one to switch between or register new devices/accounts automatically.
- `cas.authn.mfa.web-authn.core.passkey-enroll-url` (Optional; type `String`)
- `cas.authn.mfa.web-authn.core.passkey-manage-url` (Optional; type `String`)
- `cas.authn.mfa.web-authn.core.passkey-upgrade-enabled` (Optional; type `Boolean`; default `false`)
- `cas.authn.mfa.web-authn.core.qr-code-authentication-enabled` (Optional; type `Boolean`; default `false`): When enabled, allows the user to a scan a QR code on an external device and authenticate later on the primary device. This is useful in scenarios where the primary authentication device is not registered with CAS and the user has a secondary device that is registered and does not wish to use the primary device to authenticate for security or other practical reasons.
- `cas.authn.mfa.web-authn.core.trusted-device-enabled` (Optional; type `Boolean`; default `false`): Indicates whether this provider should support trusted devices.
- `cas.authn.mfa.web-authn.core.user-verification-requirement` (Optional; type `String`): Specifies the relying party's requirement for user verification during credential registration and/or authentication. User verification means the authenticator verifies the user locally, for example with a PIN, fingerprint, face recognition, or device unlock. This is stronger than simple user presence, such as touching a security key. - `REQUIRED`: user verification must be performed; the operation should fail if it is unavailable or not completed. - `PREFERRED`: user verification should be performed if possible, but the operation may still succeed without it. - `DISCOURAGED`: user verification is not requested, typically to reduce friction or avoid prompting for PIN/biometric verification. The server should validate the resulting authenticator data flags according to the configured requirement.
- `cas.authn.mfa.web-authn.core.validate-signature-counter` (Optional; type `Boolean`; default `true`): If true, finish assertion op will fail if the signature counter value in the response is not strictly greater than the stored signature counter value.
- `cas.authn.mfa.web-authn.crypto.alg` (Optional; type `String`): The signing/encryption algorithm to use.
- `cas.authn.mfa.web-authn.crypto.enabled` (Optional; type `Boolean`; default `true`): Whether crypto operations are enabled.
- `cas.authn.mfa.web-authn.crypto.encryption.key-size` (Optional; type `Integer`; default `512`): The encryption key size.
- `cas.authn.mfa.web-authn.crypto.signing.key-size` (Optional; type `Integer`; default `512`): The signing key size.
- `cas.authn.mfa.web-authn.crypto.strategy-type` (Optional; type `String`; default `ENCRYPT_AND_SIGN`): Control the cipher sequence of operations. The accepted values are: - `ENCRYPT_AND_SIGN`: Encrypt the value first, and then sign. - `SIGN_AND_ENCRYPT`: Sign the value first, and then encrypt.

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

### Bypass

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.mfa.web-authn.bypass.groovy.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.authn.mfa.web-authn.bypass.rest.url` (Required; type `String`): The endpoint URL to contact and retrieve attributes.
- `cas.authn.mfa.web-authn.bypass.authentication-attribute-name` (Optional; type `String`): Skip multifactor authentication based on designated authentication attribute names.
- `cas.authn.mfa.web-authn.bypass.authentication-attribute-value` (Optional; type `String`): Optionally, skip multifactor authentication based on designated authentication attribute values. Multiple values may be separated by a comma.
- `cas.authn.mfa.web-authn.bypass.authentication-handler-name` (Optional; type `String`): Skip multifactor authentication depending on form of primary authentication execution. Specifically, skip multifactor if the a particular authentication handler noted by its name successfully is able to authenticate credentials in the primary factor. Multiple values may be separated by a comma.
- `cas.authn.mfa.web-authn.bypass.authentication-method-name` (Optional; type `String`): Skip multifactor authentication depending on method/form of primary authentication execution. Specifically, skip multifactor if the authentication method attribute collected as part of authentication metadata matches a certain value. Multiple values may be separated by a comma.
- `cas.authn.mfa.web-authn.bypass.credential-class-type` (Optional; type `String`): Skip multifactor authentication depending on form of primary credentials. Value must equal the fully qualified class name of the credential type.
- `cas.authn.mfa.web-authn.bypass.http-request-headers` (Optional; type `String`): Skip multifactor authentication if the http request contains the defined header names. Header names may be comma-separated and can be regular expressions; values are ignored.
- `cas.authn.mfa.web-authn.bypass.http-request-remote-address` (Optional; type `String`): Skip multifactor authentication if the http request's remote address or host matches the value defined here. The value may be specified as a regular expression.
- `cas.authn.mfa.web-authn.bypass.principal-attribute-name` (Optional; type `String`): Skip multifactor authentication based on designated principal attribute names.
- `cas.authn.mfa.web-authn.bypass.principal-attribute-value` (Optional; type `String`): Optionally, skip multifactor authentication based on designated principal attribute values.
- `cas.authn.mfa.web-authn.bypass.rest.basic-auth-password` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the password for authentication.
- `cas.authn.mfa.web-authn.bypass.rest.basic-auth-username` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the username for authentication.
- `cas.authn.mfa.web-authn.bypass.rest.headers` (Optional; type `Map<String,String>`): Headers, defined as a Map, to include in the request when making the REST call. Will overwrite any header that CAS is pre-defined to send and include in the request. Key in the map should be the header name and the value in the map should be the header value.
- `cas.authn.mfa.web-authn.bypass.rest.maximum-retry-attempts` (Optional; type `Integer`; default `3`): When attempting to reach the endpoint, this setting controls the number of **retry attempts** that CAS should execute Setting this value to a zero or negative value will disable the retry policy.
- `cas.authn.mfa.web-authn.bypass.rest.method` (Optional; type `String`; default `GET`): HTTP method to use when contacting the rest endpoint. Examples include `GET, POST`, etc.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.

## Discoverable Credentials

It is possible to allow WebAuthN to act as a standalone authentication strategy for primary authentication. Using this approach, user accounts and FIDO2-enabled devices that have already registered with CAS are given the option to login using their FIDO2-enabled device for a passwordless authentication experience.

> Discoverable Credential means that the private key and associated metadata is stored in persistent memory on the authenticator, instead of encrypted and stored on the relying party server.

[Device registration](https://apereo.github.io/cas/development/mfa/FIDO2-WebAuthn-Authentication-Registration.html) can occur out of band using available CAS APIs, or by allowing users to pass through the registration flow as part of the typical multifactor authentication.

The same passkeys are also offered by [passwordless authentication](https://apereo.github.io/cas/development/authentication/Passwordless-Authentication-Passkeys.html), from the autofill menu of its username field and from its selection menu.

## Related Origins

A passkey is bound to the relying party identifier, which is set in the WebAuthn settings or else taken from the host of the CAS server name. When CAS is reached from origins whose domain differs from that identifier, list them as allowed origins in the WebAuthn settings. CAS accepts assertions from those origins and publishes them for [WebAuthn related origin requests](https://www.w3.org/TR/webauthn-3/#sctn-related-origins) at `/.well-known/webauthn`:

```json
{
  "origins": [
    "https://sso.example.org",
    "https://login.example.co.uk"
  ]
}
```

Browsers fetch this document from `https://<relying party identifier>/.well-known/webauthn`, at the root of the host and outside the CAS context path, and they only honor a limited number of distinct registrable domains in it. When CAS runs under a context path such as `/cas`, route the document onto the path CAS serves, either in the proxy that fronts CAS or with the [embedded Tomcat rewrite valve](https://apereo.github.io/cas/development/installation/Servlet-Container-Embedded-Tomcat-RewriteValve.html) registered on the engine:

```
RewriteRule ^/\.well-known/webauthn$ /cas/.well-known/webauthn [L]
```

## Passkey Endpoints

CAS publishes the [passkey endpoints metadata](https://www.w3.org/TR/passkey-endpoints/) at `/.well-known/passkey-endpoints`, which password managers and passkey providers read to send users to the pages where passkeys are created (`enroll`) and managed (`manage`):

```json
{
  "enroll": "https://sso.example.org/cas/account",
  "manage": "https://sso.example.org/cas/account"
}
```

Each URL is taken from CAS settings. When one is not set and [account management](https://apereo.github.io/cas/development/registration/Account-Management-Overview.html) is enabled, it points to the account profile, where WebAuthn devices are listed and registered; otherwise it is left out, and an empty document still tells clients that CAS supports passkeys. Like the related origins document, clients fetch it from the root of the relying party identifier’s host, so route it onto the CAS context path the same way:

```
RewriteRule ^/\.well-known/passkey-endpoints$ /cas/.well-known/passkey-endpoints [L]
```

## Signal API

After a successful WebAuthn authentication, CAS uses the [WebAuthn Signal API](https://www.w3.org/TR/webauthn-3/#sctn-signal-methods), where the browser supports it, to report the passkeys it still accepts for the user and the user’s current name and display name. Password managers and platform authenticators can then stop offering passkeys that were removed from CAS and show the account as it is named in CAS. Browsers without the Signal API ignore this.

## Passkey Provider Names

Each registration keeps the AAGUID that identifies the authenticator or passkey provider that created it. When the attestation does not name the device, which is the case for synced passkeys, the [account profile](https://apereo.github.io/cas/development/registration/Account-Management-Overview.html) shows the provider name, such as *Google Password Manager*, *1Password* or *Apple Passwords*, from a bundled snapshot of the community [passkey provider AAGUID list](https://github.com/passkeydeveloper/passkey-authenticator-aaguids). The registration page shows the same name, with the provider’s icon from that list, right after a passkey is registered. Providers that send an all-zero AAGUID, and devices registered before CAS kept the AAGUID, stay unnamed.

## Passkey Upgrades

CAS can offer a passkey to users who sign in with a username and password, without a separate enrollment step, using [WebAuthn conditional create](https://www.w3.org/TR/webauthn-3/#dom-credentialmediationrequirement-conditional). Right after the password login, CAS shows a short intermediate page that asks the browser to create a passkey for the account. If the browser’s password manager has just filled in the password for that account, it may create the passkey on its own, usually with no prompt; otherwise the request fails silently. Either way the page continues to the application within a few seconds, and browsers that do not support conditional create continue immediately.

> **Passkey Upgrades**
>
> Passkey upgrades must be explicitly enabled in CAS configuration. Then, note that this feature also only takes effect when WebAuthn is allowed for primary authentication and when untrusted attestation is allowed. Review CAS settings to ensure all features are correctly enabled.

The intermediate page is shown only when the login was completed with a password typed in that login, and the account may register another device: it has no device yet, or multiple registration is turned on. The passkey is named after the username that was typed, so that the password manager can match it with the saved password, and it belongs to the authenticated principal. It is listed with its provider’s name, such as *Google Password Manager*, or as *Passkey*.

Conditional create is supported by recent versions of Chrome with Google Password Manager and Safari with iCloud Keychain, and by many third-party password managers.
