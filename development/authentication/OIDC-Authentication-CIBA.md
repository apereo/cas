# OpenID Connect - Client-Initiated Backchannel Authentication (CIBA)

Source: https://apereo.github.io/cas/development/authentication/OIDC-Authentication-CIBA.html

Client-Initiated Backchannel Authentication (CIBA) is a new authentication flow in which relying parties, that can obtain a valid identifier for the user they want to authenticate, will be able to initiate an interaction flow to authenticate their users without having end-user interaction from the consumption device. The flow involves direct communication from the Client to CAS without redirect through the user’s browser.

- The Client shall make an “HTTP POST” request to the Backchannel Authentication Endpoint to ask for end-user authentication.
- CAS will respond immediately with a unique identifier that identifies that authentication while it tries to authenticate the user in the background.
- The Client will receive the ID Token, Access Token, and optionally Refresh Token through either the Poll, Ping, or Push modes.

Please study [the specification](https://openid.net/specs/openid-client-initiated-backchannel-authentication-core-1_0.html) to learn more.

## Configuration

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.oidc.ciba.verification.mail.attribute-name` (Required; type `List<String>`): Principal attribute names that indicates the destination email address for this message. The attributes must already be resolved and available to the CAS principal. When multiple attributes are specified, each attribute is then examined against the available CAS principal to locate the email address value, which may result in multiple emails being sent.
- `cas.authn.oidc.ciba.verification.mail.from` (Required; type `String`): Email from address.
- `cas.authn.oidc.ciba.verification.mail.subject` (Required; type `String`): Email subject line. The subject can either be defined verbatim, or it may point to a message key in the language bundle using the syntax `#{subject-language-key}`. This key should point to a valid message defined in the appropriate language bundle that is then picked up via the active locale. In case where the language code cannot resolve the real subject, a default subject value would be used.
- `cas.authn.oidc.ciba.max-time-to-live-in-seconds` (Optional; type `String`; default `PT5M`): Hard timeout to kill the ID token and expire it.
- `cas.authn.oidc.ciba.storage-name` (Optional; type `String`; default `oidcCibaRequestsCache`): The storage object name used and created by CAS to hold CIBA requests in the backing ticket registry implementation.
- `cas.authn.oidc.ciba.verification.delay` (Optional; type `String`; default `PT5S`): Number of seconds to pause and wait before sending out the verification notification to the user. Essentially, this controls how long the user must wait before the notification message is received via email, etc.
- `cas.authn.oidc.ciba.verification.mail.bcc` (Optional; type `List<String>`): Email BCC address, if any.
- `cas.authn.oidc.ciba.verification.mail.cc` (Optional; type `List<String>`): Email CC address, if any.
- `cas.authn.oidc.ciba.verification.mail.html` (Optional; type `Boolean`; default `false`): Indicate whether the message body should be evaluated as HTML text. The application of this setting depends on the email provider implementation and may not be fully supported everywhere. This is typically relevant for the default `org.springframework.mail.javamail.JavaMailSender`.
- `cas.authn.oidc.ciba.verification.mail.priority` (Optional; type `Integer`; default `1`): Set the priority (`X-Priority` header) of the message. Values: `1 (Highest)`, `2 (High)`, `3 (Normal)`, `4 (Low)`, `5 (Lowest)`. The application of this setting depends on the email provider implementation and may not be fully supported everywhere. This is typically relevant for the default `org.springframework.mail.javamail.JavaMailSender`.
- `cas.authn.oidc.ciba.verification.mail.reply-to` (Optional; type `String`): Email Reply-To address, if any.
- `cas.authn.oidc.ciba.verification.mail.text` (Optional; type `String`): Email message body. Could be plain text or a reference to an external file that would serve as a template. If specified as a path to an external file with an extension `.gtemplate`, then the email message body would be processed using the Groovy template engine. The template engine uses JSP style <% %> script and <%= %> expression syntax or GString style expressions. The variable `out` is bound to the writer that the template is being written to. If using plain text, the contents are processed for string substitution candidates using named variables. For example, you may refer to an expected url variable in the email text via `${url}`, or use `${token}` to locate the token variable. In certain cases, additional parameters are passed to the email body processor that might include authentication and/or principal attributes, the available locale, client http information, etc.
- `cas.authn.oidc.ciba.verification.mail.validate-addresses` (Optional; type `Boolean`; default `false`): Set whether to validate all addresses which get passed to this helper. The application of this setting depends on the email provider implementation and may not be fully supported everywhere. This is typically relevant for the default `org.springframework.mail.javamail.JavaMailSender`.

**Email servers**

The following settings may also need to be defined to describe the mail server settings:

- `spring.mail.default-encoding` (Third party; type `Charset`; default `UTF-8`): Default MimeMessage encoding.
- `spring.mail.host` (Third party; type `String`): SMTP server host. For instance, 'smtp.example.com'.
- `spring.mail.jndi-name` (Third party; type `String`): Session JNDI name. When set, takes precedence over other Session settings.
- `spring.mail.password` (Third party; type `String`): Login password of the SMTP server.
- `spring.mail.port` (Third party; type `Integer`): SMTP server port.
- `spring.mail.properties` (Third party; type `Map<String,String>`): Additional JavaMail Session properties.
- `spring.mail.protocol` (Third party; type `String`; default `smtp`): Protocol used by the SMTP server.
- `spring.mail.ssl.bundle` (Third party; type `String`): SSL bundle name. If set, 'mail.(protocol).ssl.socketFactory' property is set to an SSLSocketFactory obtained from the corresponding SSL bundle. Note that the STARTTLS command can use the corresponding SSLSocketFactory, even if the 'mail.(protocol).ssl.enable' property is not set.
- `spring.mail.ssl.enabled` (Third party; type `Boolean`; default `false`): Whether to enable SSL support. If enabled, 'mail.(protocol).ssl.enable' property is set to 'true'.
- `spring.mail.ssl.verify-hostname` (Third party; type `Boolean`; default `true`): Whether to enable hostname verification.
- `spring.mail.test-connection` (Third party; type `Boolean`; default `false`): Whether to test that the mail server is available on startup.
- `spring.mail.username` (Third party; type `String`): Login user of the SMTP server.

## Applications

Applications that wish to take advantage of CIBA can be registered with CAS:

```json
{
  "@class": "org.apereo.cas.services.OidcRegisteredService",
  "clientId": "client-id",
  "clientSecret": "secret",
  "serviceId": "^https://app.example.org/oidc",
  "name": "MyApplication",
  "id": 1,
  "supportedGrantTypes": [ "java.util.HashSet", [ "urn:openid:params:grant-type:ciba" ] ],
  "backchannelTokenDeliveryMode": "push",
  "backchannelClientNotificationEndpoint": "https://app.example.org/notify",
  "backchannelAuthenticationRequestSigningAlg": "",
  "backchannelUserCodeParameterSupported": false
}
```

### Signed Authentication Requests

An application may send its authentication request as a signed request object, as [the specification](https://openid.net/specs/openid-client-initiated-backchannel-authentication-core-1_0.html#signed_auth_request) allows. The request object is verified with the keys of the application that authenticated the request, whether or not the request names it with `client_id`, and must be signed with an asymmetric algorithm. It must name the application as its issuer, be addressed to CAS and carry `exp`, `iat`, `nbf` and `jti`; it may not be issued in the future, may be valid for 60 minutes at most, and is used once: its `jti` is remembered until it expires. Request objects are refused altogether when CAS is configured not to support the request parameter. A request object that does not meet these rules is refused with `invalid_request`, rather than set aside for the other parameters of the request.

### Delivery Modes

The Client will receive the ID Token, Access Token, and optionally Refresh Token through either the `poll`, `ping`, or `push` modes. This choice MUST be established when the relying party is registered with CAS.

- Poll Mode

When configured in Poll mode, the Client will poll the token endpoint to get a response with the tokens. That is, the authentication result is retrieved by the Client by polling the CAS token endpoint using the CIBA grant type. Until the user approves the request, the token endpoint answers with `authorization_pending`. An `auth_req_id` that has expired is refused with `expired_token`, and one that is unknown or has already been redeemed is refused with `invalid_grant`. An expired request is only recognized as such while CAS still holds it; once the ticket registry has removed it, it is reported as unknown.

- Ping Mode

When configured in Ping mode, CAS will send a request to a callback URI previously registered by the Client with the unique identifier returned from the Backchannel Authentication Endpoint. Upon receipt of the notification, the Client makes a request to the token endpoint to obtain the tokens.

- Push Mode

When configured in Push mode, CAS will send a `POST` request with the tokens to a callback URI previously registered by the Client.
