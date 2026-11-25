# Long Term Authentication

Source: https://apereo.github.io/cas/development/authentication/Configuring-LongTerm-Authentication.html

This feature, also known as *Remember Me*, extends the length of the SSO session beyond the typical period of hours such that users can go days or weeks without having to log in to CAS. See the [security guide](https://apereo.github.io/cas/development/planning/Security-Guide.html) for discussion of security concerns related to long term authentication.

## Policy and Deployment Considerations

While users can elect to establish a long term authentication session, the duration is established through configuration as a matter of security policy. Deployers must determine the length of long term authentication sessions by weighing convenience against security risks.

The use of long term authentication sessions dramatically increases the length of time ticket-granting tickets are stored in the ticket registry. Loss of a ticket-granting ticket corresponding to a long-term SSO session would require the user to re-authenticate to CAS. A security policy that requires that long term authentication sessions MUST NOT be terminated prior to their natural expiration would mandate a ticket registry component that provides for durable storage, such as the [JPA Ticket Registry](https://apereo.github.io/cas/development/ticketing/JPA-Ticket-Registry.html).

## Configuration

Adjust your expiration policy so that remember-me authentication requests are handled via a long-term timeout expiration policy, and other requests are handled via the CAS default SSO session expiration policy.

The following settings and properties are available from the CAS configuration catalog:

- `cas.tgc.crypto.encryption.key` (Required; type `String`): The encryption key is a string whose length is defined by the encryption key size setting.
- `cas.tgc.crypto.signing.key` (Required; type `String`): The signing key is a string whose length is defined by the signing key size setting.
- `cas.tgc.allowed-ip-addresses-pattern` (Optional; type `String`): A regular expression pattern that indicates the set of allowed IP addresses, when `#isPinToSession()` is configured. In the event that there is a mismatch between the cookie IP address and the current request-provided IP address (i.e. network switches, VPN, etc), the cookie can still be considered valid if the new IP address matches the pattern specified here.
- `cas.tgc.auto-configure-cookie-path` (Optional; type `Boolean`; default `true`): Decide if cookie paths should be automatically configured based on the application context path, when the cookie path is not configured.
- `cas.tgc.crypto.alg` (Optional; type `String`): The signing/encryption algorithm to use.
- `cas.tgc.crypto.enabled` (Optional; type `Boolean`; default `true`): Whether crypto operations are enabled.
- `cas.tgc.crypto.encryption-enabled` (Optional; type `Boolean`; default `true`)
- `cas.tgc.crypto.encryption.key-size` (Optional; type `Integer`; default `512`): The encryption key size.
- `cas.tgc.crypto.signing-enabled` (Optional; type `Boolean`; default `true`)
- `cas.tgc.crypto.signing.key-size` (Optional; type `Integer`; default `512`): The signing key size.
- `cas.tgc.crypto.strategy-type` (Optional; type `String`; default `ENCRYPT_AND_SIGN`): Control the cipher sequence of operations. The accepted values are: - `ENCRYPT_AND_SIGN`: Encrypt the value first, and then sign. - `SIGN_AND_ENCRYPT`: Sign the value first, and then encrypt.
- `cas.tgc.domain` (Optional; type `String`): Cookie domain. Specifies the domain within which this cookie should be presented. The form of the domain name is specified by RFC 2965. A domain name begins with a dot (.foo.com) and means that the cookie is visible to servers in a specified Domain Name System (DNS) zone (for example, www.foo.com, but not a.b.foo.com). By default, cookies are only returned to the server that sent them.
- `cas.tgc.geo-locate-client-session` (Optional; type `Boolean`; default `false`): When set to `true` and assuming `#isPinToSession()` is also `true`, client sessions (using the client IP address) are geo-located using a geolocation service when/if configured. The resulting session is either pinned to the client geolocation, or the default client address.
- `cas.tgc.http-only` (Optional; type `Boolean`; default `true`): true if this cookie contains the HttpOnly attribute. This means that the cookie should not be accessible to scripting engines, like javascript.
- `cas.tgc.max-age` (Optional; type `String`; default `-1`): The maximum age of the cookie, specified in seconds. By default, `-1` indicating the cookie will persist until browser shutdown. A positive value indicates that the cookie will expire after that many seconds have passed. Note that the value is the maximum age when the cookie will expire, not the cookie's current age. A negative value means that the cookie is not stored persistently and will be deleted when the Web browser exits. A zero value causes the cookie to be deleted.
- `cas.tgc.name` (Optional; type `String`): Cookie name. Constructs a cookie with a specified name and value. The name must conform to RFC 2965. That means it can contain only ASCII alphanumeric characters and cannot contain commas, semicolons, or white space or begin with a `$` character. The cookie's name cannot be changed after creation. By default, cookies are created according to the RFC 2965 cookie specification. Cookie names are automatically calculated assigned by CAS at runtime, and there is usually no need to customize the name or assign it a different value unless a special use case warrants the change.
- `cas.tgc.path` (Optional; type `String`): Cookie path. Specifies a path for the cookie to which the client should return the cookie. The cookie is visible to all the pages in the directory you specify, and all the pages in that directory's subdirectories. A cookie's path must include the servlet that set the cookie, for example, /catalog, which makes the cookie visible to all directories on the server under /catalog. Consult RFC 2965 (available on the Internet) for more information on setting path names for cookies.
- `cas.tgc.pin-to-session` (Optional; type `Boolean`; default `true`): When generating cookie values, determine whether the value should be compounded and signed with the properties of the current session, such as IP address, user-agent, etc. Turning off session pinning is not exactly ideal. This is a barrier to prevent cookie replay attacks. A cookie that was created with an IP given a certain location cannot be replayed back in a different browser with a different IP from a different location. There may however be legitimate reasons to turn it off and relax this after careful reviews, if you intend to support a type of user who intends to switch their IP quite frequently. A classic example is the type of user that is on VPN at the time of login, but decides to resume their work a few hours later off VPN or at a different location.
- `cas.tgc.remember-me-max-age` (Optional; type `String`; default `P14D`): If remember-me is enabled, specifies the maximum age of the cookie.
- `cas.tgc.same-site-policy` (Optional; type `String`): If a cookie is only intended to be accessed in a first party context, the developer has the option to apply one of settings `SameSite=Lax` or `SameSite=Strict` or `SameSite=None` to prevent external access. To safeguard more websites and their users, the new secure-by-default model assumes all cookies should be protected from external access unless otherwise specified. Developers must use a new cookie setting, `SameSite=None`, to designate cookies for cross-site access. When the `SameSite=None` attribute is present, an additional `Secure` attribute is used so cross-site cookies can only be accessed over HTTPS connections. Accepted values are: - `Lax` - `Strict` - `None` - `Off`: Disable the generation of the SameSite cookie attribute altogether. - Path to a Groovy script that is able to generate the SameSite cookie attribute dynamically. - Fully qualified name of a class that implements `org.apereo.cas.web.cookie.CookieSameSitePolicy`
- `cas.tgc.secure` (Optional; type `Boolean`; default `true`): True if sending this cookie should be restricted to a secure protocol, or false if the it can be sent using any protocol.
- `cas.ticket.tgt.remember-me.enabled` (Optional; type `Boolean`; default `false`): Flag to indicate whether remember-me facility is enabled.
- `cas.ticket.tgt.remember-me.supported-ip-addresses` (Optional; type `String`): Regular expression that, when defined, forces CAS to create a remember-me authentication session if the current client ip (remote) address matches this pattern. If a match is not found, remember-me is ignored. If left undefined, remember-me authentication will proceed with the default CAS behavior.
- `cas.ticket.tgt.remember-me.supported-user-agents` (Optional; type `String`): Regular expression that, when defined, forces CAS to create a remember-me authentication session if the current user-agent matches this pattern. If a match is not found, remember-me is ignored. If left undefined, remember-me authentication will proceed with the default CAS behavior.
- `cas.ticket.tgt.remember-me.time-to-kill-in-seconds` (Optional; type `String`; default `P14D`): Time in seconds after which remember-me enabled SSO session will be destroyed.

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
