# Multifactor Authentication (MFA)

Source: https://apereo.github.io/cas/development/mfa/Configuring-Multifactor-Authentication.html

CAS provides support for a variety of multifactor authentication providers and options, while allowing one to design their own. The secondary authentication factor always kicks in *after* the primary step and existing authentication sessions will be asked to step-up to the needed multifactor authentication factor, should the request or trigger require it. The satisfied authentication context is communicated back to the application as well to denote a successful multifactor authentication event.

At a minimum, you need answer the following questions:

- Which provider(s) are we using for multifactor authentication?
- How and for whom are we triggering multifactor authentication?

## Supported Providers

The following multifactor providers are supported by CAS.

| Provider | Id | Instructions |
| --- | --- | --- |
| Duo Security | `mfa-duo` | [See this guide](https://apereo.github.io/cas/development/mfa/DuoSecurity-Authentication.html). |
| YubiKey | `mfa-yubikey` | [See this guide](https://apereo.github.io/cas/development/mfa/YubiKey-Authentication.html). |
| RSA/RADIUS | `mfa-radius` | [See this guide](https://apereo.github.io/cas/development/mfa/RADIUS-Authentication.html). |
| Google Authenticator | `mfa-gauth` | [See this guide](https://apereo.github.io/cas/development/mfa/GoogleAuthenticator-Authentication.html). |
| FIDO2 WebAuthN | `mfa-webauthn` | [See this guide](https://apereo.github.io/cas/development/mfa/FIDO2-WebAuthn-Authentication.html). |
| CAS Simple | `mfa-simple` | [See this guide](https://apereo.github.io/cas/development/mfa/Simple-Multifactor-Authentication.html). |
| Twilio | `mfa-twilio` | [See this guide](https://apereo.github.io/cas/development/mfa/Twilio-Multifactor-Authentication.html). |
| Inwebo | `mfa-inwebo` | [See this guide](https://apereo.github.io/cas/development/mfa/Inwebo-Authentication.html). |
| Custom | Custom | [See this guide](https://apereo.github.io/cas/development/mfa/Custom-MFA-Authentication.html). |

### Comparison

| Provider | What the user does | External service | Registrations kept by CAS | Phishing-resistant |
| --- | --- | --- | --- | --- |
| [Duo Security](https://apereo.github.io/cas/development/mfa/DuoSecurity-Authentication.html) | Approves a push, takes a call or enters a passcode | Duo | No, Duo keeps them | Depends on the Duo method |
| [FIDO2 WebAuthn](https://apereo.github.io/cas/development/mfa/FIDO2-WebAuthn-Authentication.html) | Uses a passkey or security key | None | [Yes](https://apereo.github.io/cas/development/mfa/FIDO2-WebAuthn-Authentication-Registration.html) | Yes |
| [Google Authenticator](https://apereo.github.io/cas/development/mfa/GoogleAuthenticator-Authentication.html) | Enters a code from a TOTP app | None | Yes, in JSON, JPA, LDAP, MongoDb, Redis, DynamoDb or REST | No |
| [YubiKey](https://apereo.github.io/cas/development/mfa/YubiKey-Authentication.html) | Touches a YubiKey that types a one-time password | Yubico validation service | Yes, in JSON, JPA, MongoDb, Redis, DynamoDb or REST | No |
| [RSA/RADIUS](https://apereo.github.io/cas/development/mfa/RADIUS-Authentication.html) | Enters a code checked by a RADIUS server | Your RADIUS server, such as RSA | No | No |
| [CAS Simple](https://apereo.github.io/cas/development/mfa/Simple-Multifactor-Authentication.html) | Enters a code CAS sent by email, SMS or notification | An email or SMS provider | No, contact details come from user attributes | No |
| [Twilio](https://apereo.github.io/cas/development/mfa/Twilio-Multifactor-Authentication.html) | Enters a code sent by SMS, WhatsApp or voice call | Twilio Verify | No, the recipient comes from a user attribute | No |
| [Inwebo](https://apereo.github.io/cas/development/mfa/Inwebo-Authentication.html) | Approves the login with Inwebo (TrustBuilder) | Inwebo | No, Inwebo keeps them | Depends on the Inwebo method |

Phishing-resistant factors are bound to the CAS site and cannot be replayed through a look-alike page. Codes and push approvals can be relayed by an attacker who controls such a page.

> **Microsoft Entra Multifactor Authentication**
>
> Microsoft has removed the ability for external SSO servers and identity providers to use Microsoft Entra multifactor authentication as a standalone MFA solution. To use Microsoft Entra multifactor authentication, you must also have all your users authenticate using Microsoft Entra ID SSO. You may want to route authentication requests to Microsoft Entra ID SSO using the delegated authentication features of CAS.

> **Remember**
>
> Support for Google Authenticator is not about supporting the mobile application itself. This is about supporting the TOTP algorithm that is used by the application to generate time-based one-time passwords. Any compliant mobile application that can generate TOTP codes can be used with CAS including LastPass, Microsoft Authenticator, Authy, etc.

## Configuration

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.mfa.core.provider-selection.cookie.crypto.encryption.key` (Required; type `String`): The encryption key is a string whose length is defined by the encryption key size setting.
- `cas.authn.mfa.core.provider-selection.cookie.crypto.signing.key` (Required; type `String`): The signing key is a string whose length is defined by the signing key size setting.
- `cas.authn.mfa.core.provider-selection.provider-selector-groovy-script.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.authn.mfa.core.authentication-context-attribute` (Optional; type `String`; default `authnContextClass`): Attribute returned in the final CAS validation payload that indicates the authentication context class satisfied in the event of a multifactor authentication attempt.
- `cas.authn.mfa.core.content-type` (Optional; type `String`; default `application/cas`): Content-type that is expected to be specified by non-web clients such as curl, etc in the event that the provider supports variations of non-browser based MFA. The value is treated as a regular expression.
- `cas.authn.mfa.core.global-failure-mode` (Optional; type `BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes`; default `closed`): Defines the global failure mode for the entire deployment. This is meant to be used a shortcut to define the policy globally rather than per application. Applications registered with CAS can still define a failure mode and override the global. Available values are as follows: - `OPEN`: Disallow MFA, proceed with authentication but don't communicate MFA to the RP. - `CLOSED`: Disallow MFA, block with authentication. - `PHANTOM`: Disallow MFA, proceed with authentication and communicate MFA to the RP. - `NONE`: Do not check for failure at all. - `UNDEFINED`: The default one indicating that no failure mode is set at all.
- `cas.authn.mfa.core.provider-selection.cookie.allowed-ip-addresses-pattern` (Optional; type `String`): A regular expression pattern that indicates the set of allowed IP addresses, when `#isPinToSession()` is configured. In the event that there is a mismatch between the cookie IP address and the current request-provided IP address (i.e. network switches, VPN, etc), the cookie can still be considered valid if the new IP address matches the pattern specified here.
- `cas.authn.mfa.core.provider-selection.cookie.auto-configure-cookie-path` (Optional; type `Boolean`; default `true`): Decide if cookie paths should be automatically configured based on the application context path, when the cookie path is not configured.
- `cas.authn.mfa.core.provider-selection.cookie.crypto.alg` (Optional; type `String`): The signing/encryption algorithm to use.
- `cas.authn.mfa.core.provider-selection.cookie.crypto.enabled` (Optional; type `Boolean`; default `true`): Whether crypto operations are enabled.
- `cas.authn.mfa.core.provider-selection.cookie.crypto.encryption.key-size` (Optional; type `Integer`; default `512`): The encryption key size.
- `cas.authn.mfa.core.provider-selection.cookie.crypto.signing.key-size` (Optional; type `Integer`; default `512`): The signing key size.
- `cas.authn.mfa.core.provider-selection.cookie.crypto.strategy-type` (Optional; type `String`; default `ENCRYPT_AND_SIGN`): Control the cipher sequence of operations. The accepted values are: - `ENCRYPT_AND_SIGN`: Encrypt the value first, and then sign. - `SIGN_AND_ENCRYPT`: Sign the value first, and then encrypt.
- `cas.authn.mfa.core.provider-selection.cookie.domain` (Optional; type `String`): Cookie domain. Specifies the domain within which this cookie should be presented. The form of the domain name is specified by RFC 2965. A domain name begins with a dot (.foo.com) and means that the cookie is visible to servers in a specified Domain Name System (DNS) zone (for example, www.foo.com, but not a.b.foo.com). By default, cookies are only returned to the server that sent them.
- `cas.authn.mfa.core.provider-selection.cookie.enabled` (Optional; type `Boolean`; default `true`): Whether MFA selection should be remembered with cookies.
- `cas.authn.mfa.core.provider-selection.cookie.geo-locate-client-session` (Optional; type `Boolean`; default `false`): When set to `true` and assuming `#isPinToSession()` is also `true`, client sessions (using the client IP address) are geo-located using a geolocation service when/if configured. The resulting session is either pinned to the client geolocation, or the default client address.
- `cas.authn.mfa.core.provider-selection.cookie.http-only` (Optional; type `Boolean`; default `true`): true if this cookie contains the HttpOnly attribute. This means that the cookie should not be accessible to scripting engines, like javascript.
- `cas.authn.mfa.core.provider-selection.cookie.max-age` (Optional; type `String`; default `-1`): The maximum age of the cookie, specified in seconds. By default, `-1` indicating the cookie will persist until browser shutdown. A positive value indicates that the cookie will expire after that many seconds have passed. Note that the value is the maximum age when the cookie will expire, not the cookie's current age. A negative value means that the cookie is not stored persistently and will be deleted when the Web browser exits. A zero value causes the cookie to be deleted.
- `cas.authn.mfa.core.provider-selection.cookie.name` (Optional; type `String`): Cookie name. Constructs a cookie with a specified name and value. The name must conform to RFC 2965. That means it can contain only ASCII alphanumeric characters and cannot contain commas, semicolons, or white space or begin with a `$` character. The cookie's name cannot be changed after creation. By default, cookies are created according to the RFC 2965 cookie specification. Cookie names are automatically calculated assigned by CAS at runtime, and there is usually no need to customize the name or assign it a different value unless a special use case warrants the change.
- `cas.authn.mfa.core.provider-selection.cookie.path` (Optional; type `String`): Cookie path. Specifies a path for the cookie to which the client should return the cookie. The cookie is visible to all the pages in the directory you specify, and all the pages in that directory's subdirectories. A cookie's path must include the servlet that set the cookie, for example, /catalog, which makes the cookie visible to all directories on the server under /catalog. Consult RFC 2965 (available on the Internet) for more information on setting path names for cookies.
- `cas.authn.mfa.core.provider-selection.cookie.pin-to-session` (Optional; type `Boolean`; default `true`): When generating cookie values, determine whether the value should be compounded and signed with the properties of the current session, such as IP address, user-agent, etc. Turning off session pinning is not exactly ideal. This is a barrier to prevent cookie replay attacks. A cookie that was created with an IP given a certain location cannot be replayed back in a different browser with a different IP from a different location. There may however be legitimate reasons to turn it off and relax this after careful reviews, if you intend to support a type of user who intends to switch their IP quite frequently. A classic example is the type of user that is on VPN at the time of login, but decides to resume their work a few hours later off VPN or at a different location.
- `cas.authn.mfa.core.provider-selection.cookie.same-site-policy` (Optional; type `String`): If a cookie is only intended to be accessed in a first party context, the developer has the option to apply one of settings `SameSite=Lax` or `SameSite=Strict` or `SameSite=None` to prevent external access. To safeguard more websites and their users, the new secure-by-default model assumes all cookies should be protected from external access unless otherwise specified. Developers must use a new cookie setting, `SameSite=None`, to designate cookies for cross-site access. When the `SameSite=None` attribute is present, an additional `Secure` attribute is used so cross-site cookies can only be accessed over HTTPS connections. Accepted values are: - `Lax` - `Strict` - `None` - `Off`: Disable the generation of the SameSite cookie attribute altogether. - Path to a Groovy script that is able to generate the SameSite cookie attribute dynamically. - Fully qualified name of a class that implements `org.apereo.cas.web.cookie.CookieSameSitePolicy`
- `cas.authn.mfa.core.provider-selection.cookie.secure` (Optional; type `Boolean`; default `true`): True if sending this cookie should be restricted to a secure protocol, or false if the it can be sent using any protocol.
- `cas.authn.mfa.core.provider-selection.provider-selection-enabled` (Optional; type `Boolean`; default `false`): In the event that multiple multifactor authentication providers are determined for a multifactor authentication transaction, this setting will allow one to interactively choose a provider out of the list of available providers. A trigger may be designed to support more than one provider, and rather than letting CAS auto-determine the selected provider via scripts or ranking strategies, this method puts the choice back onto the user to decide which provider makes the most sense at any given time.
- `cas.authn.mfa.core.provider-selection.provider-selection-optional` (Optional; type `Boolean`; default `false`): If provider selection is enabled, this setting will allow the user to skip the provider selection step and proceed with authentication without having to select an MFA provider.

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

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.

## Triggers

Multifactor authentication can be activated via a number of triggers. To learn more, [please see this guide](https://apereo.github.io/cas/development/mfa/Configuring-Multifactor-Authentication-Triggers.html).

## Bypass Rules

Each multifactor provider is equipped with options to allow for MFA bypass. To learn more, [please see this guide](https://apereo.github.io/cas/development/mfa/Configuring-Multifactor-Authentication-Bypass.html).

## Failure Modes

CAS will consult the current configuration in the event that the provider being requested is unreachable to determine how to proceed. To learn more, [please see this guide](https://apereo.github.io/cas/development/mfa/Configuring-Multifactor-Authentication-FailureModes.html).

## Provider Selection

There are options and controls available to allow CAS to select a multifactor authentication provider, in case multiple triggers and conditions activate multiple providers. To learn more, [please see this guide](https://apereo.github.io/cas/development/mfa/Multifactor-Authentication-ProviderSelection.html).
