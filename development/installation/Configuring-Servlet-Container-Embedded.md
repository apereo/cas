# Embedded Servlet Container

Source: https://apereo.github.io/cas/development/installation/Configuring-Servlet-Container-Embedded.html

Note that CAS itself ships with a number of embedded containers that allow the platform to be self-contained as much as possible. These embedded containers are an integral part of the CAS software, are maintained and updated usually for every release and surely are meant to and can be used in production deployments. You **DO NOT** need to, but can if you want to, configure and deploy to an externally configured container.

## Configuration

The following settings and properties are available from the CAS configuration catalog:

- `server.connection-timeout` (Third party; type `Duration`; deprecation `ERROR`, no replacement)
- `server.port` (Third party; type `Integer`; default `8080`): Server HTTP port.
- `server.servlet.application-display-name` (Third party; type `String`; default `application`): Display name of the application.
- `server.servlet.context-parameters` (Third party; type `Map<String,String>`): Servlet context init parameters.
- `server.servlet.context-path` (Third party; type `String`): Context path of the application.
- `server.servlet.encoding.charset` (Third party; type `Charset`; deprecation `ERROR`, replaced by `spring.servlet.encoding.charset`): Charset of HTTP requests and responses. Added to the Content-Type header if not set explicitly.
- `server.servlet.encoding.enabled` (Third party; type `Boolean`; deprecation `ERROR`, replaced by `spring.servlet.encoding.enabled`; default `true`): Whether to enable http encoding support.
- `server.servlet.encoding.force` (Third party; type `Boolean`; deprecation `ERROR`, replaced by `spring.servlet.encoding.force`; default `false`): Whether to force the encoding to the configured charset on HTTP requests and responses.
- `server.servlet.encoding.force-request` (Third party; type `Boolean`; deprecation `ERROR`, replaced by `spring.servlet.encoding.force-request`; default `true`): Whether to force the encoding to the configured charset on HTTP requests. Defaults to true when force has not been specified.
- `server.servlet.encoding.force-response` (Third party; type `Boolean`; deprecation `ERROR`, replaced by `spring.servlet.encoding.force-response`; default `false`): Whether to force the encoding to the configured charset on HTTP responses.
- `server.servlet.encoding.mapping` (Third party; type `Map<Locale,Charset>`): Mapping of locale to charset for response encoding.
- `server.servlet.jsp.class-name` (Third party; type `String`; default `org.apache.jasper.servlet.JspServlet`): Class name of the servlet to use for JSPs. If registered is true and this class * is on the classpath then it will be registered.
- `server.servlet.jsp.init-parameters` (Third party; type `Map<String,String>`): Init parameters used to configure the JSP servlet.
- `server.servlet.jsp.registered` (Third party; type `Boolean`; default `true`): Whether the JSP servlet is registered.
- `server.servlet.path` (Third party; type `String`; deprecation `ERROR`, replaced by `spring.mvc.servlet.path`; default `/`): Path of the main dispatcher servlet.
- `server.servlet.register-default-servlet` (Third party; type `Boolean`; default `false`): Whether to register the default Servlet with the container.
- `server.servlet.session.cookie.comment` (Third party; deprecation `ERROR`, no replacement): Comment for the cookie.
- `server.servlet.session.cookie.domain` (Third party; type `String`): Domain for the cookie.
- `server.servlet.session.cookie.http-only` (Third party; type `Boolean`): Whether to use "HttpOnly" cookies for the cookie.
- `server.servlet.session.cookie.max-age` (Third party; type `Duration`): Maximum age of the cookie. If a duration suffix is not specified, seconds will be used. A positive value indicates when the cookie expires relative to the current time. A value of 0 means the cookie should expire immediately. A negative value means no "Max-Age".
- `server.servlet.session.cookie.name` (Third party; type `String`): Name for the cookie.
- `server.servlet.session.cookie.partitioned` (Third party; type `Boolean`): Whether the generated cookie carries the Partitioned attribute.
- `server.servlet.session.cookie.path` (Third party; type `String`): Path of the cookie.
- `server.servlet.session.cookie.same-site` (Third party; type `Cookie.SameSite`): SameSite setting for the cookie.
- `server.servlet.session.cookie.secure` (Third party; type `Boolean`): Whether to always mark the cookie as secure.
- `server.servlet.session.persistent` (Third party; type `Boolean`; default `false`): Whether to persist session data between restarts.
- `server.servlet.session.store-dir` (Third party; type `File`): Directory used to store session data.
- `server.servlet.session.timeout` (Third party; type `Duration`; default `30m`): Session timeout. If a duration suffix is not specified, seconds will be used.
- `server.servlet.session.tracking-modes` (Third party; type `Set<Session.SessionTrackingMode>`): Session tracking modes.
- `server.ssl.bundle` (Third party; type `String`): Name of a configured SSL bundle.
- `server.ssl.certificate` (Third party; type `String`): Path to a PEM-encoded SSL certificate file.
- `server.ssl.certificate-private-key` (Third party; type `String`): Path to a PEM-encoded private key file for the SSL certificate.
- `server.ssl.ciphers` (Third party; type `String[]`): Supported SSL ciphers.
- `server.ssl.client-auth` (Third party; type `Ssl.ClientAuth`): Client authentication mode. Requires a trust store.
- `server.ssl.enabled` (Third party; type `Boolean`; default `true`): Whether to enable SSL support.
- `server.ssl.enabled-protocols` (Third party; type `String[]`): Enabled SSL protocols.
- `server.ssl.key-alias` (Third party; type `String`): Alias that identifies the key in the key store.
- `server.ssl.key-password` (Third party; type `String`): Password used to access the key in the key store.
- `server.ssl.key-store` (Third party; type `String`): Path to the key store that holds the SSL certificate (typically a jks file).
- `server.ssl.key-store-password` (Third party; type `String`): Password used to access the key store.
- `server.ssl.key-store-provider` (Third party; type `String`): Provider for the key store.
- `server.ssl.key-store-type` (Third party; type `String`): Type of the key store.
- `server.ssl.protocol` (Third party; type `String`; default `TLS`): SSL protocol to use.
- `server.ssl.server-name-bundles` (Third party; type `List<Ssl.ServerNameSslBundle>`): Mapping of host names to SSL bundles for SNI configuration.
- `server.ssl.trust-certificate` (Third party; type `String`): Path to a PEM-encoded SSL certificate authority file.
- `server.ssl.trust-certificate-private-key` (Third party; type `String`): Path to a PEM-encoded private key file for the SSL certificate authority.
- `server.ssl.trust-store` (Third party; type `String`): Trust store that holds SSL certificates.
- `server.ssl.trust-store-password` (Third party; type `String`): Password used to access the trust store.
- `server.ssl.trust-store-provider` (Third party; type `String`): Provider for the trust store.
- `server.ssl.trust-store-type` (Third party; type `String`): Type of the trust store.
- `server.use-forward-headers` (Third party; type `Boolean`; deprecation `ERROR`, replaced by `server.forward-headers-strategy`)

## Execution

The CAS web application, once built, may be deployed in place with the embedded container via the following command:

```bash
java -jar /path/to/cas.war
```

Additionally, it is also possible to run CAS as a fully executable web application:

```bash
# chmod +x /path/to/cas.war
/path/to/cas.war
```

This is achieved via the build process of the deployment overlay where a launch script is *inserted* at the beginning of the web application artifact. If you wish to see and examine the script, run the following commands:

```bash
 # X is the number of lines from the beginning of the file
 head -n X /path/to.cas.war
```

Note that running CAS as a standalone and fully executable web application is supported on most Linux and OS X distributions. Other platforms such as Windows may require custom configuration.

The following embedded servlet containers are available:

| Option | Reference |
| --- | --- |
| Apache Tomcat | Please see [this guide](https://apereo.github.io/cas/development/installation/Configuring-Servlet-Container-Embedded-Tomcat.html). |
| Jetty | Please see [this guide](https://apereo.github.io/cas/development/installation/Configuring-Servlet-Container-Embedded-Jetty.html). |
