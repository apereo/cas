# SAML2 Attribute Friendly Name

Source: https://apereo.github.io/cas/development/installation/Configuring-SAML2-Attribute-FriendlyName.html

Attribute friendly names can be specified per relying party in the service registry, as well as globally via CAS settings. If there is no friendly name defined for the attribute, the attribute name will be used instead in its place. Note that the name of the attribute is one that is designed to be released to the service provider, specially if the original attribute is *mapped* to a different name.

```json
{
  "@class": "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId" : "the-entity-id-of-the-sp",
  "name": "SAML Service",
  "metadataLocation" : "../../sp-metadata.xml",
  "id": 100001,
  "attributeFriendlyNames": {
    "@class": "java.util.HashMap",
    "urn:oid:2.5.4.42": "friendly-name-to-use"
  }
}
```

You may also have the option to define attribute friendly names globally via CAS properties.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.saml-idp.core.entity-id` (Required; type `String`; default `https://cas.example.org/idp`): The SAML entity id for the deployment.
- `cas.authn.saml-idp.core.session-replication.cookie.crypto.encryption.key` (Required; type `String`): The encryption key is a string whose length is defined by the encryption key size setting.
- `cas.authn.saml-idp.core.session-replication.cookie.crypto.signing.key` (Required; type `String`): The signing key is a string whose length is defined by the signing key size setting.
- `cas.authn.saml-idp.core.attribute-friendly-names` (Optional; type `List<String>`): A mapping of attribute names to their friendly names, defined globally. Example might be `urn:oid:1.3.6.1.4.1.5923.1.1.1.6->eduPersonPrincipalName`.
- `cas.authn.saml-idp.core.attribute-query-profile-enabled` (Optional; type `Boolean`; default `false`): Indicates whether attribute query profile is enabled. Enabling this setting would allow CAS to record SAML responses and have them be made available later for attribute lookups.
- `cas.authn.saml-idp.core.context.authentication-context-class-mappings` (Optional; type `List<String>`): A mapping of authentication context class refs. This is where specific authentication context classes are referenced and mapped to providers that CAS may support mainly for, i.e. MFA purposes. Example might be `urn:oasis:names:tc:SAML:2.0:ac:classes:SomeClassName->mfa-duo`. In delegated authentication scenarios, this can also be a mapping of authentication context class refs, when CAS is proxying/delegating authentication to an external SAML2 identity provider. The requested authentication context as submitted by the service provider is first received by CAS, and then gets mapped to a context class that is passed onto the external identity provider. For example, you might have a scenario where a SAML2 service provider would submit `https://refeds.org/profile/mfa` to CAS, and CAS would translate that to `http://schemas.microsoft.com/claims/multipleauthn` to ultimate route the authentication request to Azure. If no mapping is found, the original context is passed as is. Example might be `https://refeds.org/profile/mfa->http://schemas.microsoft.com/claims/multipleauthn`.
- `cas.authn.saml-idp.core.context.default-authentication-context-class` (Optional; type `String`; default `urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport`): The default authentication context class to include in the response if none is specified via the service.
- `cas.authn.saml-idp.core.session-replication.cookie.allowed-ip-addresses-pattern` (Optional; type `String`): A regular expression pattern that indicates the set of allowed IP addresses, when `#isPinToSession()` is configured. In the event that there is a mismatch between the cookie IP address and the current request-provided IP address (i.e. network switches, VPN, etc), the cookie can still be considered valid if the new IP address matches the pattern specified here.
- `cas.authn.saml-idp.core.session-replication.cookie.auto-configure-cookie-path` (Optional; type `Boolean`; default `true`): Decide if cookie paths should be automatically configured based on the application context path, when the cookie path is not configured. @deprecated since 7.3.0
- `cas.authn.saml-idp.core.session-replication.cookie.crypto.alg` (Optional; type `String`): The signing/encryption algorithm to use.
- `cas.authn.saml-idp.core.session-replication.cookie.crypto.enabled` (Optional; type `Boolean`; default `true`): Whether crypto operations are enabled.
- `cas.authn.saml-idp.core.session-replication.cookie.crypto.encryption.key-size` (Optional; type `Integer`; default `512`): The encryption key size.
- `cas.authn.saml-idp.core.session-replication.cookie.crypto.signing.key-size` (Optional; type `Integer`; default `512`): The signing key size.
- `cas.authn.saml-idp.core.session-replication.cookie.crypto.strategy-type` (Optional; type `String`; default `ENCRYPT_AND_SIGN`): Control the cipher sequence of operations. The accepted values are: - `ENCRYPT_AND_SIGN`: Encrypt the value first, and then sign. - `SIGN_AND_ENCRYPT`: Sign the value first, and then encrypt.
- `cas.authn.saml-idp.core.session-replication.cookie.domain` (Optional; type `String`): Cookie domain. Specifies the domain within which this cookie should be presented. The form of the domain name is specified by RFC 2965. A domain name begins with a dot (.foo.com) and means that the cookie is visible to servers in a specified Domain Name System (DNS) zone (for example, www.foo.com, but not a.b.foo.com). By default, cookies are only returned to the server that sent them.
- `cas.authn.saml-idp.core.session-replication.cookie.geo-locate-client-session` (Optional; type `Boolean`; default `false`): When set to `true` and assuming `#isPinToSession()` is also `true`, client sessions (using the client IP address) are geo-located using a geolocation service when/if configured. The resulting session is either pinned to the client geolocation, or the default client address.
- `cas.authn.saml-idp.core.session-replication.cookie.http-only` (Optional; type `Boolean`; default `true`): true if this cookie contains the HttpOnly attribute. This means that the cookie should not be accessible to scripting engines, like javascript.
- `cas.authn.saml-idp.core.session-replication.cookie.max-age` (Optional; type `String`; default `-1`): The maximum age of the cookie, specified in seconds. By default, `-1` indicating the cookie will persist until browser shutdown. A positive value indicates that the cookie will expire after that many seconds have passed. Note that the value is the maximum age when the cookie will expire, not the cookie's current age. A negative value means that the cookie is not stored persistently and will be deleted when the Web browser exits. A zero value causes the cookie to be deleted.
- `cas.authn.saml-idp.core.session-replication.cookie.name` (Optional; type `String`): Cookie name. Constructs a cookie with a specified name and value. The name must conform to RFC 2965. That means it can contain only ASCII alphanumeric characters and cannot contain commas, semicolons, or white space or begin with a `$` character. The cookie's name cannot be changed after creation. By default, cookies are created according to the RFC 2965 cookie specification. Cookie names are automatically calculated assigned by CAS at runtime, and there is usually no need to customize the name or assign it a different value unless a special use case warrants the change.
- `cas.authn.saml-idp.core.session-replication.cookie.path` (Optional; type `String`): Cookie path. Specifies a path for the cookie to which the client should return the cookie. The cookie is visible to all the pages in the directory you specify, and all the pages in that directory's subdirectories. A cookie's path must include the servlet that set the cookie, for example, /catalog, which makes the cookie visible to all directories on the server under /catalog. Consult RFC 2965 (available on the Internet) for more information on setting path names for cookies.
- `cas.authn.saml-idp.core.session-replication.cookie.pin-to-session` (Optional; type `Boolean`; default `true`): When generating cookie values, determine whether the value should be compounded and signed with the properties of the current session, such as IP address, user-agent, etc. Turning off session pinning is not exactly ideal. This is a barrier to prevent cookie replay attacks. A cookie that was created with an IP given a certain location cannot be replayed back in a different browser with a different IP from a different location. There may however be legitimate reasons to turn it off and relax this after careful reviews, if you intend to support a type of user who intends to switch their IP quite frequently. A classic example is the type of user that is on VPN at the time of login, but decides to resume their work a few hours later off VPN or at a different location.
- `cas.authn.saml-idp.core.session-replication.cookie.same-site-policy` (Optional; type `String`): If a cookie is only intended to be accessed in a first party context, the developer has the option to apply one of settings `SameSite=Lax` or `SameSite=Strict` or `SameSite=None` to prevent external access. To safeguard more websites and their users, the new secure-by-default model assumes all cookies should be protected from external access unless otherwise specified. Developers must use a new cookie setting, `SameSite=None`, to designate cookies for cross-site access. When the `SameSite=None` attribute is present, an additional `Secure` attribute is used so cross-site cookies can only be accessed over HTTPS connections. Accepted values are: - `Lax` - `Strict` - `None` - `Off`: Disable the generation of the SameSite cookie attribute altogether. - Path to a Groovy script that is able to generate the SameSite cookie attribute dynamically. - Fully qualified name of a class that implements `org.apereo.cas.web.cookie.CookieSameSitePolicy`
- `cas.authn.saml-idp.core.session-replication.cookie.secure` (Optional; type `Boolean`; default `true`): True if sending this cookie should be restricted to a secure protocol, or false if the it can be sent using any protocol.
- `cas.authn.saml-idp.core.session-replication.replicate-sessions` (Optional; type `Boolean`; default `false`): Indicates whether profiles and other session data, collected as part of authentication flows and protocol requests that are kept by the container session, should be replicated across the cluster using CAS and its own ticket registry. Without this option, profile data and other related pieces of information should be manually replicated via means and libraries outside of CAS. @deprecated since 7.3.0
- `cas.authn.saml-idp.core.session-storage-type` (Optional; type `SessionStorageTypes`; default `http`): Indicates whether saml requests, and other session data, collected as part of SAML flows and requests that are kept by the container http session, local storage, or should be replicated across the cluster. Available values are as follows: - `HTTP`: Authentication requests, and other session data collected as part of authentication protocol flows and requests are kept in the http servlet session that is local to the server. - `BROWSER_STORAGE`: Authentication requests, and other session data that the authentication protocol flow itself stores, are kept in the client browser's storage, signed and encrypted, and expire with the lifetime of transient session tickets; other session data never leaves the server. All interactions require client-side read/write operations to restore the session from the browser. Browsers that cannot use local or session storage keep the same payload in cookies instead. - `TICKET_REGISTRY`: Authentication requests, and other session data collected as part of authentication protocol flows and requests are tracked as CAS tickets in the registry and replicated across the entire cluster as tickets. This state is tied to the user's agent/browser using a special cookie that would be used to locate and restore that state. The cookie content may be signed and encrypted.

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
