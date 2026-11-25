# Security Guide

Source: https://apereo.github.io/cas/development/planning/Security-Guide.html

CAS is security software that provides secure Web-based single sign-on to Web-based applications. Single sign-on provides a win/win in terms of security and convenience: it reduces password exposure to a single, trusted credential broker while transparently providing access to multiple services without repetitious logins. The use of CAS generally improves the security environment, but there are several CAS configuration, policy, and deployment concerns that should be considered to achieve suitable security.

> **Reporting Issues**
>
> The security team asks that you please **DO NOT** create publicly-viewable issues or posts to discuss what you may consider a security vulnerability. To report issues properly and learn about how responses are produced, please [see this guide](https://apereo.github.io/cas/developer/Sec-Vuln-Response.html).

## System Security Considerations

Infrastructure security matters to consider may include the following.

### Secure Transport (https)

All communication with the CAS server MUST occur over a secure channel (i.e. TLSv1). There are two primary justifications for this requirement:

1. The authentication process requires transmission of security credentials.
2. The CAS ticket-granting ticket is a bearer token.

Since the disclosure of either data would allow impersonation attacks, it’s vitally important to secure the communication channel between CAS clients and the CAS server.

Practically, it means that all CAS urls must use HTTPS, but it **also** means that all connections from the CAS server to the application must be done using HTTPS:

- when the generated service ticket is sent back to the application on the “service” url
- when a proxy callback url is called.

The following settings and properties are available from the CAS configuration catalog:

- `cas.http-client.allow-local-urls` (Optional; type `Boolean`; default `true`): Whether CAS should accept local URLs. For example `http(s)://localhost/logout`.
- `cas.http-client.async-timeout` (Optional; type `String`; default `PT5S`): Indicates timeout for async operations.
- `cas.http-client.authority-validation-reg-ex-case-sensitive` (Optional; type `Boolean`; default `true`): Whether the regular expression specified with `#authorityValidationRegex` should be handled as case-sensitive (`true`) or case-insensitive (`false`). If no `#authorityValidationRegex` is set, this value does not have any effect.
- `cas.http-client.authority-validation-regex` (Optional; type `String`): If specified the regular expression will be used to validate the url's authority.
- `cas.http-client.connection-timeout` (Optional; type `String`; default `PT5S`): Connection timeout for all operations that reach out to URL endpoints.
- `cas.http-client.default-headers` (Optional; type `Map<String,String>`): The default headers to use for any HTTP connection. This is defined as map, where the key is the header name and the value is the header value that should be sent along with request.
- `cas.http-client.host-name-verifier` (Optional; type `String`; default `default`): Enable hostname verification when attempting to contact URL endpoints. May also be set to `none` to disable verification.
- `cas.http-client.proxy-host` (Optional; type `String`): Send requests via a proxy; define the hostname.
- `cas.http-client.proxy-port` (Optional; type `Integer`; default `0`): Send requests via a proxy; define the proxy port. Negative/zero values should deactivate the proxy configuration for the http client.
- `cas.http-client.response-timeout` (Optional; type `String`; default `PT5S`): Determines the timeout until arrival of a response from the opposite endpoint. A timeout value of zero is interpreted as an infinite timeout. Please note that response timeout may be unsupported by HTTP transports with message multiplexing.
- `cas.http-client.socket-timeout` (Optional; type `String`; default `PT5S`): Determines the default socket timeout value for I/O operations.
- `cas.http-client.truststore.file` (Optional; type `Resource`): The CAS local truststore resource to contain certificates to the CAS deployment. In the event that local certificates are to be imported into the CAS running environment, a local truststore is provided by CAS to improve portability of configuration across environments.
- `cas.http-client.truststore.psw` (Optional; type `String`; default `changeit`): The truststore password.
- `cas.http-client.truststore.type` (Optional; type `String`): Truststore type used to create a SSL context for http client.

### Connections to Dependent Systems

CAS commonly requires connections to other systems such as LDAP directories, databases, and caching services. We generally recommend to use secure transport (SSL/TLS, IPSec) to those systems where possible, but there may be compensating controls that make secure transport unnecessary. Private networks and corporate networks with strict access controls are common exceptions, but secure transport is recommended nonetheless. Client certification validation can be another good solution for LDAP to bring sufficient security.

As stated previously, connections to other systems must be secured. But if the CAS server is deployed on several nodes, the same applies to the CAS server itself. If a cache-based ticket registry runs without any security issue on a single CAS server, synchronization can become a security problem when using multiple nodes if the network is not protected.

## Deployment-Driven Security Features

CAS supports a number of features that can be leveraged to implement various security policies. The following features are provided through CAS configuration and CAS client integration. Note that many features are available out of the box, while others require explicit setup

### Forced Authentication

Many CAS clients and supported protocols support the concept of forced authentication whereby a user must re-authenticate to access a particular service. The CAS protocols support forced authentication via the *renew* parameter. Forced authentication provides additional assurance in the identity of the principal of an SSO session since the user must verify his or her credentials prior to access. Forced authentication is suitable for services where higher security is desired or mandated. Typically forced authentication is configured on a per-service basis, but the [service management](https://apereo.github.io/cas/development/planning/Security-Guide.html#service-management) facility provides some support for implementing forced authentication as a matter of centralized security policy. Forced authentication may be combined with [multi-factor authentication](https://apereo.github.io/cas/development/mfa/Configuring-Multifactor-Authentication.html) features to implement arbitrary service-specific access control policy.

### Passive Authentication

Some CAS protocols support passive authentication where access to a CAS-protected service is granted anonymously when requested. The CASv2 and CASv3 protocols support this capability via the *gateway* feature. Passive authentication complements forced authentication; where forced authentication requires authentication to access a service, passive authentication permits service access, albeit anonymously, without authentication.

### Proxy Authentication

Proxy authentication, or delegated authentication, provides a powerful, important, and potentially security-improving feature of CAS. Proxy authentication is supported by the CASv2 and CASv3 protocols and is mediated by proxy tickets that are requested by a service on behalf of a user; thus the service proxies authentication for the user. Proxy authentication is commonly used in cases where a service cannot interact directly with the user and as an alternative to replaying end-user credentials to a service.

However, proxy tickets carry risk in that services accepting proxy tickets are responsible for validating the proxy chain (the list of services through which the end-user’s authentication have been delegated to arrive at the ticket validating service). Services can opt out of accepting proxy tickets entirely (and avoid responsibility for validating proxy chains) by validating tickets against the /serviceValidate validation endpoint, but experience has shown it’s easy to be confused about this and configure to unintentionally use the /proxyValidate endpoint yet not scrutinize any proxy chains that appear in the ticket validation response. Thus proxy authentication requires careful configuration for proper security controls; it is recommended to disable proxy authentication components at the CAS server if proxy authentication is not needed.

Historically any service could obtain a proxy-granting ticket and from it a proxy ticket to access any other service. In other words, the security model is decentralized rather than centralized. The service management facility affords some centralized control of proxy authentication by exposing a proxy authentication flag that can enabled or disabled on a per-service basis. By default registered services are not granted proxy authentication capability.

### Credential Caching and Replay

The *ClearPass* extension provides a mechanism to capture primary authentication credentials, cache them (encrypted), and replay on demand as needed to access legacy services. While [proxy authentication](https://apereo.github.io/cas/development/planning/Security-Guide.html#proxy-authentication) is recommended in lieu of password replay, it may be required to integrate legacy services with CAS. See the [ClearPass](https://apereo.github.io/cas/development/integration/ClearPass.html) documentation for detailed information.

### Service Management

The service management facility provides a number of service-specific configuration controls that affect security policy and provide some support for centralized security policy. (Note that CAS has historically supported the decentralized security policy model.) Some highlights of service management controls:

- Authorized services
- Forced authentication
- Attribute release
- Proxy authentication control
- Theme control
- Service authorization control
- Multi-factor service access policy

The service management facility is comprised of a service registry containing one or more registered services, each of which specifies the management controls above. The service registry can be controlled via static configuration files, a Web user interface, or both. See the [Service Management](https://apereo.github.io/cas/development/services/Service-Management.html) section for more information.

> **Authorized Services**
>
> As a security best practice, it is **strongly** recommended to limit the service management facility to only include the list of known applications that are authorized to use CAS. Leaving the management interface open for all applications may create an opportunity for security attacks.

### SSO Cookie Encryption

A ticket-granting cookie is an HTTP cookie set by CAS upon the establishment of a single sign-on session. The cookie value is by default encrypted and signed via settings defined in CAS properties. While sample data is provided for initial deployments, these keys **MUST** be regenerated per your specific environment. Please [see this guide](https://apereo.github.io/cas/development/authentication/Configuring-SSO.html) for more info.

### Password Management Secure Links

Account password reset requests are handled via a secured link that is sent to the registered email address of the user. The link is available only within a defined time window and the request is properly signed and encrypted by CAS. While sample data is provided for initial deployments, these keys **MUST** be regenerated per your specific environment.

Please [see this guide](https://apereo.github.io/cas/development/installation/Password-Policy-Enforcement.html) for more info.

### Protocol Ticket Encryption

Protocol tickets that are issued by CAS and shared with other applications such as service tickets may optionally go through a signing/encryption process. Even though the CAS server will always cross check ticket validity and expiration policy, this may be forced as an extra check to ensure tickets in transit to other applications are not tampered with and remain to be authentic. While sample data is provided for initial deployments, these keys **MUST** be regenerated per your specific environment.

> **Pay Attention**
>
> Encrypting and signing a generated ticket will, depending on the encryption method and algorithm used, increase the generated ticket length. Not all CAS clients are equipped to handle lengthy ticket strings and may get upset with you. Evaluate existing integrations before turning this on and consider whether this feature is truly needed for your deployment.

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

### Ticket Registry Encryption

Secure ticket replication as it regards clustered CAS deployments may be required to ensure generated tickets by CAS are not tampered with in transit. CAS covers this issue by allowing tickets to be natively encrypted and signed. While sample data is provided for initial deployments, these keys **MUST** be regenerated per your specific environment. Please [see this guide](https://apereo.github.io/cas/development/installation/Ticket-Registry-Replication-Encryption.html) for more info.

### Ticket Expiration Policies

Ticket expiration policies are a primary mechanism for implementing security policy. Ticket expiration policy allows control of some important aspects of CAS SSO session behavior:

- SSO session duration (sliding expiration, absolute)
- Ticket reuse

See the [Configuring Ticketing Components](https://apereo.github.io/cas/development/ticketing/Configuring-Ticketing-Components.html) section for a detailed discussion of the various expiration policies and configuration instructions.

### Single Sign-Out

Single sign-out, or single log-out (SLO), is a feature by which CAS services are notified of the termination of a CAS SSO session with the expectation that services terminate access for the SSO session owner. While single sign-out can improve security, it is fundamentally a best-effort facility and may not actually terminate access to all services consumed during an SSO session. The following compensating controls may be used to improve risks associated with single sign-out shortcomings:

- Require forced authentication for sensitive services
- Reduce application session timeouts
- Reduce SSO session duration

SLO can happen in two ways: from the CAS server (back-channel logout) and/or from the browser (front-channel logout). For back-channel logout, the SLO process relies on the `SimpleHttpClient` class which has a threads pool: its size must be defined to properly treat all the logout requests. Additional not-already-processed logout requests are temporarily stored in a queue before being sent: its size is defined to 20% of the global capacity of the threads pool and can be adjusted. Both sizes are critical settings of the CAS system and their values should never exceed the real capacity of the CAS server.

### Login Throttling

CAS supports a policy-driven feature to limit successive failed authentication attempts to help prevent brute force and denial of service attacks. The feature is beneficial in environments where back-end authentication stores lack equivalent features. In cases where this support is available in underlying systems, we encourage using it instead of CAS features; the justification is that enabling support in underlying systems provides the feature in all dependent systems including CAS. See the [login throttling configuration](https://apereo.github.io/cas/development/authentication/Configuring-Authentication-Throttling.html) section for further information.

### Credential Encryption

To learn how sensitive CAS settings can be secured via encryption, [please review this guide](https://apereo.github.io/cas/development/configuration/Configuration-Properties-Security.html).

### CAS Security Filter

The CAS project provides a number of a blunt security filters suitable for patching-in-place Java CAS server and Java CAS client deployments vulnerable to certain request parameter based bad-CAS-protocol-input attacks. The filters are configured to sanitize authentication request parameters and reject the request if it is not compliant with the CAS protocol in the event that for instance, a parameter is repeated multiple times, includes multiple values, contains unacceptable values, etc.

It is **STRONGLY** recommended that all CAS deployments be evaluated and include this configuration if necessary to prevent protocol attacks in situations where the CAS container and environment are unable to block malicious and badly-configured requests.

#### CORS

CAS provides first-class support for enabling HTTP access control (CORS). One application of CORS is when a resource makes a cross-origin HTTP request when it requests a resource from a different domain than the one which the first resource itself serves. This should help more with CAS-enabled applications are accessed via XHR/Ajax requests.

The following settings and properties are available from the CAS configuration catalog:

- `cas.http-web-request.cors.enabled` (Required; type `Boolean`; default `false`): Whether CORS should be enabled for http requests.
- `cas.http-web-request.cors.allow-credentials` (Optional; type `Boolean`; default `true`): The Access-Control-Allow-Credentials header Indicates whether or not the response to the request can be exposed when the credentials flag is true. When used as part of a response to a preflight request, this indicates whether or not the actual request can be made using credentials. Note that simple GET requests are not preflighted, and so if a request is made for a resource with credentials, if this header is not returned with the resource, the response is ignored by the browser and not returned to web content.
- `cas.http-web-request.cors.allow-headers` (Optional; type `List<String>`): The Access-Control-Allow-Headers header is used in response to a preflight request to indicate which HTTP headers can be used when making the actual request. Default is everything.
- `cas.http-web-request.cors.allow-methods` (Optional; type `List<String>`): The Access-Control-Allow-Methods header specifies the method or methods allowed when accessing the resource. This is used in response to a pre-flight request. The conditions under which a request is pre-flighted are discussed above. Default is everything.
- `cas.http-web-request.cors.allow-origin-patterns` (Optional; type `List<String>`): Comma-separated list of origin patterns to allow. Unlike allowed origins which only supports `*`, origin patterns are more flexible (for example `https://*.example.com`) and can be used when credentials are allowed. When no allowed origin patterns or allowed origins are set, CORS support is disabled.
- `cas.http-web-request.cors.allow-origins` (Optional; type `List<String>`): The Origin header indicates the origin of the cross-site access request or preflight request. The origin is a URI indicating the server from which the request initiated. When credentials are allowed, '*' cannot be used and origin patterns should be configured instead. It does not include any path information, but only the server name.
- `cas.http-web-request.cors.exposed-headers` (Optional; type `List<String>`): The Access-Control-Expose-Headers header lets a server accept headers that browsers are allowed to access.
- `cas.http-web-request.cors.max-age` (Optional; type `Long`; default `3600`): The Access-Control-Max-Age header indicates how long the results of a preflight request can be cached.

#### Security Response Headers

As part of the CAS Security Filter, the CAS project automatically provides the necessary configuration to insert HTTP Security headers into the web response to prevent against HSTS, XSS, X-FRAME and other attacks. These settings are presently on by default.

The following settings and properties are available from the CAS configuration catalog:

- `cas.http-web-request.header.enabled` (Required; type `Boolean`; default `true`): Allow CAS to inject and enforce http security headers via an http filter that are outlined here for caching, HSTS, etc.
- `cas.http-web-request.header.cache` (Optional; type `Boolean`; default `true`): When true, will inject the following headers into the response for non-static resources. ``` Cache-Control: no-cache, no-store, max-age=0, must-revalidate Pragma: no-cache Expires: 0 ```
- `cas.http-web-request.header.cache-control-static-resources` (Optional; type `String`; default `css|js|png|txt|jpg|ico|jpeg|bmp|gif`): Files with these extensions are considered static, so they will be cached by browsers. The value is part of a RegEx.
- `cas.http-web-request.header.content-security-policy` (Optional; type `String`; default `script-src 'self' 'unsafe-inline' 'unsafe-eval' https://www.googletagmanager.com https://www.google.com; object-src 'none'; worker-src 'self' blob: 'unsafe-inline';`): Helps you reduce XSS risks on modern browsers by declaring what dynamic resources are allowed to load via a HTTP Header. Header value is made up of one or more directives. Multiple directives are separated with a semicolon. @nonce@ is a specific value which is replaced by a generated random value saved as the request attribute 'contentSecurityPolicyGeneratedNonce'.
- `cas.http-web-request.header.hsts` (Optional; type `Boolean`; default `true`): When true, will inject the following headers into the response: `Strict-Transport-Security: ...`.
- `cas.http-web-request.header.hsts-options` (Optional; type `String`; default `max-age=15768000 ; includeSubDomains`): Control the value of the `Strict-Transport-Security` header.
- `cas.http-web-request.header.xcontent` (Optional; type `Boolean`; default `true`): When true, will inject the following headers into the response: `X-Content-Type-Options: nosniff`.
- `cas.http-web-request.header.xframe` (Optional; type `Boolean`; default `true`): When true, will inject the following headers into the response: `X-Frame-Options: DENY`.
- `cas.http-web-request.header.xframe-options` (Optional; type `String`; default `DENY`): Will inject values into the `X-Frame-Options` header into the response.
- `cas.http-web-request.header.xss` (Optional; type `Boolean`; default `true`): When true, will inject the following headers into the response: `X-XSS-Protection: 1; mode=block`.
- `cas.http-web-request.header.xss-options` (Optional; type `String`; default `1; mode=block`): Will inject values into the `X-XSS-Protection` header into the response.

### Spring Webflow Sessions

The CAS project uses Spring Webflow to manage and orchestrate the authentication process. The conversational state of the webflow used by CAS is managed by the client which is then passed and tracked throughout various states of the authentication process. This state must be secured and encrypted to prevent session hijacking. While CAS provides default encryption settings out of the box, it is **STRONGLY** recommended that [all CAS deployments](https://apereo.github.io/cas/development/webflow/Webflow-Customization.html) be evaluated prior to production deployments and regenerate this configuration to prevent attacks.

### Long Term Authentication

The long term authentication feature, commonly referred to as “Remember Me”, is selected (usually via checkbox) on the CAS login form to avoid re-authentication for an extended period of time. Long term authentication allows users to elect additional convenience at the expense of reduced security. The extent of reduced security is a function of the characteristics of the device used to establish a CAS SSO session. A long-term SSO session established from a device owned or operated by a single user is marginally less secure than a standard CAS SSO session. The only real concern would be the increased lifetime and resulting increased exposure of the CAS ticket-granting ticket. Establishing a long-term CAS SSO session from a shared device, on the other hand, may dramatically reduce security. The likelihood of artifacts from previous SSO sessions affecting subsequent SSO sessions established by other users, even in the face of single sign-out, may increase the likelihood of impersonation. While there is no feasible mitigation for improving security of long-term SSO sessions on a shared device, educating users on the inherent risks may improve overall security.

It is important to note that forced authentication supersedes long term authentication, thus if a service were configured for forced authentication, authentication would be required for service access even in the context of a long-term session.

Long term authentication support must be explicitly enabled through [configuration and UI customization](https://apereo.github.io/cas/development/authentication/Configuring-LongTerm-Authentication.html) during the installation process. Thus deployers choose to offer long-term authentication support, and when available users may elect to use it via selection on the CAS login form.

### Warning Before Redirect

CAS supports optional notification of service access during an established SSO session. By default CAS transparently requests tickets needed for service access and presents them to the target service for validation, whereby upon successful validation access to the service is permitted. In most cases this happens nearly instantly and the user is not aware of the CAS authentication process required to access CAS-enabled services. There may be some security benefit to awareness of this process, and CAS supports a *warn* flag that may be selected by the user on the CAS login screen to provide an interstitial notification page that is displayed prior to accessing a service. By default the notification page offers the user an option to proceed with CAS authentication or abort by navigating away from the target service.
