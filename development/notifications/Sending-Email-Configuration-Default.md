# Sending Email - Default

Source: https://apereo.github.io/cas/development/notifications/Sending-Email-Configuration-Default.html

By default, CAS sends email through an SMTP server using the JavaMail API.

The default strategy uses the `JavaMail` API which provides a platform-independent and protocol-independent framework to build mail and messaging applications, primarily using SMTP:

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
