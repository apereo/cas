# Configure Service Expiration Policy

Source: https://apereo.github.io/cas/development/services/Configuring-Service-Expiration-Policy.html

An application registered with CAS may be assigned an optional expiration policy that controls the lifetime of the registration. Once the service is deemed expired, it will automatically be disabled or removed from the CAS registry and [relevant contacts](https://apereo.github.io/cas/development/services/Configuring-Service-Contacts.html) defined and assigned to the service will be notified via email or text messages.

The following settings and properties are available from the CAS configuration catalog:

- `cas.service-registry.mail.attribute-name` (Required; type `List<String>`): Principal attribute names that indicates the destination email address for this message. The attributes must already be resolved and available to the CAS principal. When multiple attributes are specified, each attribute is then examined against the available CAS principal to locate the email address value, which may result in multiple emails being sent.
- `cas.service-registry.mail.from` (Required; type `String`): Email from address.
- `cas.service-registry.mail.subject` (Required; type `String`): Email subject line. The subject can either be defined verbatim, or it may point to a message key in the language bundle using the syntax `#{subject-language-key}`. This key should point to a valid message defined in the appropriate language bundle that is then picked up via the active locale. In case where the language code cannot resolve the real subject, a default subject value would be used.
- `cas.service-registry.sms.attribute-name` (Required; type `List<String>`): Principal attribute names that indicates the destination phone number for this SMS message. The attribute must already be resolved and available to the CAS principal.
- `cas.service-registry.sms.from` (Required; type `String`): The from address for the message.
- `cas.service-registry.sms.text` (Required; type `String`): The body of the SMS message.
- `cas.service-registry.mail.bcc` (Optional; type `List<String>`): Email BCC address, if any.
- `cas.service-registry.mail.cc` (Optional; type `List<String>`): Email CC address, if any.
- `cas.service-registry.mail.html` (Optional; type `Boolean`; default `false`): Indicate whether the message body should be evaluated as HTML text. The application of this setting depends on the email provider implementation and may not be fully supported everywhere. This is typically relevant for the default `org.springframework.mail.javamail.JavaMailSender`.
- `cas.service-registry.mail.priority` (Optional; type `Integer`; default `1`): Set the priority (`X-Priority` header) of the message. Values: `1 (Highest)`, `2 (High)`, `3 (Normal)`, `4 (Low)`, `5 (Lowest)`. The application of this setting depends on the email provider implementation and may not be fully supported everywhere. This is typically relevant for the default `org.springframework.mail.javamail.JavaMailSender`.
- `cas.service-registry.mail.reply-to` (Optional; type `String`): Email Reply-To address, if any.
- `cas.service-registry.mail.text` (Optional; type `String`): Email message body. Could be plain text or a reference to an external file that would serve as a template. If specified as a path to an external file with an extension `.gtemplate`, then the email message body would be processed using the Groovy template engine. The template engine uses JSP style <% %> script and <%= %> expression syntax or GString style expressions. The variable `out` is bound to the writer that the template is being written to. If using plain text, the contents are processed for string substitution candidates using named variables. For example, you may refer to an expected url variable in the email text via `${url}`, or use `${token}` to locate the token variable. In certain cases, additional parameters are passed to the email body processor that might include authentication and/or principal attributes, the available locale, client http information, etc.
- `cas.service-registry.mail.validate-addresses` (Optional; type `Boolean`; default `false`): Set whether to validate all addresses which get passed to this helper. The application of this setting depends on the email provider implementation and may not be fully supported everywhere. This is typically relevant for the default `org.springframework.mail.javamail.JavaMailSender`.

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

A sample JSON file follows:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^https://.+",
  "name" : "sample service",
  "id" : 100,
  "expirationPolicy": {
    "@class": "org.apereo.cas.services.DefaultRegisteredServiceExpirationPolicy",
    "deleteWhenExpired": true,
    "notifyWhenDeleted": false,
    "notifyWhenExpired": false,
    "expirationDate": "2017-10-05"
  }
}
```

The following settings are available by default for expiration policies:

| Field | Description |
| --- | --- |
| `expirationDate` | The date on which the registration record is deemed expired. The expiration date may be specified in `2011-12-03T10:15:30`, `09/24/1980 04:30 PM`, `09/24/2014 6:30 AM`, `09/24/2013 18:45`, `09/24/2017` or `2017-10-25` formats. |
| `deleteWhenExpired` | When `true`, removes the application from the CAS service registry if and when expired. Otherwise the application record will be marked as disabled. |
| `notifyWhenDeleted` | Notifies [contacts](https://apereo.github.io/cas/development/services/Configuring-Service-Contacts.html) of the application via email or text, assuming valid contacts with email addresses or phone numbers are defined and CAS is configured to send [email messages](https://apereo.github.io/cas/development/notifications/Sending-Email-Configuration.html) or [SMS notifications](https://apereo.github.io/cas/development/notifications/SMS-Messaging-Configuration.html). The notification is only sent if the application is expired and is about to be deleted from the registry. |
| `notifyWhenExpired` | Notifies [contacts](https://apereo.github.io/cas/development/services/Configuring-Service-Contacts.html) of the application via email or text, assuming valid contacts with email addresses or phone numbers are defined and CAS is configured to send [email messages](https://apereo.github.io/cas/development/notifications/Sending-Email-Configuration.html) or [SMS notifications](https://apereo.github.io/cas/development/notifications/SMS-Messaging-Configuration.html). The notification is only sent if the application is expired. |
