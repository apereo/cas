# Device Fingerprint - Multifactor Authentication Trusted Device/Browser

Source: https://apereo.github.io/cas/development/mfa/Multifactor-TrustedDevice-Authentication-DeviceFingerprint.html

In order to distinguish trusted devices from each other we need to calculate a device fingerprint that uniquely identifies individual devices. Calculation of this device fingerprint can utilize a combination of multiple components from the request.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.mfa.trusted.device-fingerprint.core.component-separator` (Optional; type `String`; default `@`): Component Separator for device fingerprints.

## Strategies

Device fingerprint can be calculated using the following ways:

- Client IP address
- Randomly generated cookie plus the client IP (default)
- [GeoLocation address](https://apereo.github.io/cas/development/authentication/GeoTracking-Authentication-Requests.html). You do need to ensure CAS is allowed to [ask and process geodata](https://apereo.github.io/cas/development/authentication/Configuring-Authentication-Events.html) provided by the browser.
- User-agent header
- Browser fingerprint calculated via client-side JavaScript and collected during authentication attempts.

**Client IP**

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.mfa.trusted.device-fingerprint.client-ip.enabled` (Optional; type `Boolean`; default `false`): Is this component enabled or not.
- `cas.authn.mfa.trusted.device-fingerprint.client-ip.order` (Optional; type `Integer`; default `0`): Indicates the order of components when generating a device fingerprint.

**Cookie**

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.mfa.trusted.device-fingerprint.cookie.crypto.encryption.key` (Required; type `String`): The encryption key is a string whose length is defined by the encryption key size setting.
- `cas.authn.mfa.trusted.device-fingerprint.cookie.crypto.signing.key` (Required; type `String`): The signing key is a string whose length is defined by the signing key size setting.
- `cas.authn.mfa.trusted.device-fingerprint.cookie.crypto.alg` (Optional; type `String`): The signing/encryption algorithm to use.
- `cas.authn.mfa.trusted.device-fingerprint.cookie.crypto.enabled` (Optional; type `Boolean`; default `true`): Whether crypto operations are enabled.
- `cas.authn.mfa.trusted.device-fingerprint.cookie.crypto.encryption.key-size` (Optional; type `Integer`; default `512`): The encryption key size.
- `cas.authn.mfa.trusted.device-fingerprint.cookie.crypto.signing.key-size` (Optional; type `Integer`; default `512`): The signing key size.
- `cas.authn.mfa.trusted.device-fingerprint.cookie.crypto.strategy-type` (Optional; type `String`; default `ENCRYPT_AND_SIGN`): Control the cipher sequence of operations. The accepted values are: - `ENCRYPT_AND_SIGN`: Encrypt the value first, and then sign. - `SIGN_AND_ENCRYPT`: Sign the value first, and then encrypt.
- `cas.authn.mfa.trusted.device-fingerprint.cookie.domain` (Optional; type `String`): Cookie domain. Specifies the domain within which this cookie should be presented. The form of the domain name is specified by RFC 2965. A domain name begins with a dot (.foo.com) and means that the cookie is visible to servers in a specified Domain Name System (DNS) zone (for example, www.foo.com, but not a.b.foo.com). By default, cookies are only returned to the server that sent them.
- `cas.authn.mfa.trusted.device-fingerprint.cookie.enabled` (Optional; type `Boolean`; default `true`): Is this component enabled or not.
- `cas.authn.mfa.trusted.device-fingerprint.cookie.http-only` (Optional; type `Boolean`; default `true`): true if this cookie contains the HttpOnly attribute. This means that the cookie should not be accessible to scripting engines, like javascript.
- `cas.authn.mfa.trusted.device-fingerprint.cookie.max-age` (Optional; type `String`; default `-1`): The maximum age of the cookie, specified in seconds. By default, `-1` indicating the cookie will persist until browser shutdown. A positive value indicates that the cookie will expire after that many seconds have passed. Note that the value is the maximum age when the cookie will expire, not the cookie's current age. A negative value means that the cookie is not stored persistently and will be deleted when the Web browser exits. A zero value causes the cookie to be deleted.
- `cas.authn.mfa.trusted.device-fingerprint.cookie.name` (Optional; type `String`): Cookie name. Constructs a cookie with a specified name and value. The name must conform to RFC 2965. That means it can contain only ASCII alphanumeric characters and cannot contain commas, semicolons, or white space or begin with a `$` character. The cookie's name cannot be changed after creation. By default, cookies are created according to the RFC 2965 cookie specification. Cookie names are automatically calculated assigned by CAS at runtime, and there is usually no need to customize the name or assign it a different value unless a special use case warrants the change.
- `cas.authn.mfa.trusted.device-fingerprint.cookie.order` (Optional; type `Integer`; default `1`): Indicates the order of components when generating a device fingerprint.
- `cas.authn.mfa.trusted.device-fingerprint.cookie.path` (Optional; type `String`): Cookie path. Specifies a path for the cookie to which the client should return the cookie. The cookie is visible to all the pages in the directory you specify, and all the pages in that directory's subdirectories. A cookie's path must include the servlet that set the cookie, for example, /catalog, which makes the cookie visible to all directories on the server under /catalog. Consult RFC 2965 (available on the Internet) for more information on setting path names for cookies.
- `cas.authn.mfa.trusted.device-fingerprint.cookie.same-site-policy` (Optional; type `String`): If a cookie is only intended to be accessed in a first party context, the developer has the option to apply one of settings `SameSite=Lax` or `SameSite=Strict` or `SameSite=None` to prevent external access. To safeguard more websites and their users, the new secure-by-default model assumes all cookies should be protected from external access unless otherwise specified. Developers must use a new cookie setting, `SameSite=None`, to designate cookies for cross-site access. When the `SameSite=None` attribute is present, an additional `Secure` attribute is used so cross-site cookies can only be accessed over HTTPS connections. Accepted values are: - `Lax` - `Strict` - `None` - `Off`: Disable the generation of the SameSite cookie attribute altogether. - Path to a Groovy script that is able to generate the SameSite cookie attribute dynamically. - Fully qualified name of a class that implements `org.apereo.cas.web.cookie.CookieSameSitePolicy`
- `cas.authn.mfa.trusted.device-fingerprint.cookie.secure` (Optional; type `Boolean`; default `true`): True if sending this cookie should be restricted to a secure protocol, or false if the it can be sent using any protocol.

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

**User Agent**

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.mfa.trusted.device-fingerprint.user-agent.enabled` (Optional; type `Boolean`; default `false`): Is this component enabled or not.
- `cas.authn.mfa.trusted.device-fingerprint.user-agent.order` (Optional; type `Integer`; default `0`): Indicates the order of components when generating a device fingerprint.

**Browser**

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.mfa.trusted.device-fingerprint.browser.enabled` (Optional; type `Boolean`; default `false`): Is this component enabled or not.
- `cas.authn.mfa.trusted.device-fingerprint.browser.order` (Optional; type `Integer`; default `0`): Indicates the order of components when generating a device fingerprint.

**GeoLocation**

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.mfa.trusted.device-fingerprint.geolocation.enabled` (Optional; type `Boolean`; default `false`): Is this component enabled or not.
- `cas.authn.mfa.trusted.device-fingerprint.geolocation.order` (Optional; type `Integer`; default `0`): Indicates the order of components when generating a device fingerprint.
