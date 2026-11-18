# Simple Multifactor Authentication - Registration

Source: https://apereo.github.io/cas/development/mfa/Simple-Multifactor-Authentication-Registration.html

By default, registration is expected to have occurred as an out-of-band process. Ultimately, CAS expects to fetch the necessary attributes from configured attribute sources to determine communications channels for email and/or SMS. The adopter is expected to have populated user records with enough information to indicate a phone number and/or email address where CAS could then be configured to fetch and examine those attributes to share generated tokens.

In the event that no contact information can be found for the user, CAS will inform the user and allows the flow to be redirected to an external application where onboarding and device registration is expected to occur.

## Just-In-Time Registration

CAS also supports a just-in-time registration strategy where users are allowed to register their email address for multifactor authentication. In the event that no contact information can be found for the user, CAS will present an option for the user to register their email address. A one-time-token is then sent to the user’s email address and upon verification, the user is allowed to proceed with the multifactor authentication flow.

> **Usage**
>
> It goes without saying that there are security considerations with the just-in-time device registration flow. When enabled, system security and user protection is largely the responsibility of the initial authentication event and the protection of primary user credentials.

This option is explicitly turned off by default and can be enabled via CAS settings notably by defining a regular expression that controls the format of the allowed email addresses that may be registered.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.mfa.simple.mail.attribute-name` (Required; type `List<String>`): Principal attribute names that indicates the destination email address for this message. The attributes must already be resolved and available to the CAS principal. When multiple attributes are specified, each attribute is then examined against the available CAS principal to locate the email address value, which may result in multiple emails being sent.
- `cas.authn.mfa.simple.mail.from` (Required; type `String`): Email from address.
- `cas.authn.mfa.simple.mail.subject` (Required; type `String`): Email subject line. The subject can either be defined verbatim, or it may point to a message key in the language bundle using the syntax `#{subject-language-key}`. This key should point to a valid message defined in the appropriate language bundle that is then picked up via the active locale. In case where the language code cannot resolve the real subject, a default subject value would be used.
- `cas.authn.mfa.simple.mail.accepted-email-pattern` (Optional; type `Pattern`): The regular expression that controls which email addresses users are allowed to register during the multifactor authentication flow. If no pattern is defined (default), registration flow will be disabled. An example email address might be `^[a-zA-Z0-9._%+-]+@example\.org$`.
- `cas.authn.mfa.simple.mail.bcc` (Optional; type `List<String>`): Email BCC address, if any.
- `cas.authn.mfa.simple.mail.cc` (Optional; type `List<String>`): Email CC address, if any.
- `cas.authn.mfa.simple.mail.html` (Optional; type `Boolean`; default `false`): Indicate whether the message body should be evaluated as HTML text. The application of this setting depends on the email provider implementation and may not be fully supported everywhere. This is typically relevant for the default `org.springframework.mail.javamail.JavaMailSender`.
- `cas.authn.mfa.simple.mail.priority` (Optional; type `Integer`; default `1`): Set the priority (`X-Priority` header) of the message. Values: `1 (Highest)`, `2 (High)`, `3 (Normal)`, `4 (Low)`, `5 (Lowest)`. The application of this setting depends on the email provider implementation and may not be fully supported everywhere. This is typically relevant for the default `org.springframework.mail.javamail.JavaMailSender`.
- `cas.authn.mfa.simple.mail.reply-to` (Optional; type `String`): Email Reply-To address, if any.
- `cas.authn.mfa.simple.mail.text` (Optional; type `String`): Email message body. Could be plain text or a reference to an external file that would serve as a template. If specified as a path to an external file with an extension `.gtemplate`, then the email message body would be processed using the Groovy template engine. The template engine uses JSP style <% %> script and <%= %> expression syntax or GString style expressions. The variable `out` is bound to the writer that the template is being written to. If using plain text, the contents are processed for string substitution candidates using named variables. For example, you may refer to an expected url variable in the email text via `${url}`, or use `${token}` to locate the token variable. In certain cases, additional parameters are passed to the email body processor that might include authentication and/or principal attributes, the available locale, client http information, etc.
- `cas.authn.mfa.simple.mail.validate-addresses` (Optional; type `Boolean`; default `false`): Set whether to validate all addresses which get passed to this helper. The application of this setting depends on the email provider implementation and may not be fully supported everywhere. This is typically relevant for the default `org.springframework.mail.javamail.JavaMailSender`.

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

Moreover, you **MUST** define and implement the following `Bean` component in your CAS configuration that is ultimately tasked to receive the user’s registration details and update the relevant user record.

```java
@Bean
public CasSimpleMultifactorAuthenticationAccountService casSimpleMultifactorAuthenticationAccountService() {
    return new MySimpleMultifactorAuthenticationAccountService();
}
```

> **Remember**
>
> Device registration flow is limited to registering email addresses. Support for registering other forms of contact information may be worked out in future releases.

[See this guide](https://apereo.github.io/cas/development/configuration/Configuration-Management-Extensions.html) to learn more about how to register configurations into the CAS runtime.
