# Jetty - Embedded Servlet Container Configuration

Source: https://apereo.github.io/cas/development/installation/Configuring-Servlet-Container-Embedded-Jetty.html

Run CAS on an embedded Jetty server instead of Apache Tomcat by building the overlay with the Jetty web application module.

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
        Including this module in the CAS WAR overlay is optional and unnecessary. This module is automatically included
        and bundled with the CAS server distribution and there are alternative options baked in to allow one to replace
        this module with another. The entry below is listed for reference only.
    */
    implementation "org.apereo.cas:cas-server-webapp-jetty"
}
```

### Embedded Jetty Container

The following settings and properties are available from the CAS configuration catalog:

- `cas.server.jetty.sni-host-check` (Optional; type `Boolean`; default `true`): Server Name Indication is an extension of the Transport Layer Security (TLS) protocol, which allows a client to indicate which hostname it is attempting to connect to at the start of the handshaking process. This is particularly useful when a server hosts multiple domains with different SSL certificates on a single IP address. Setting this setting to `false` would mean that the Jetty server will not strictly require clients to send an SNI extension during the SSL/TLS handshake and disables host name checking for Server Name Indication (SNI) during SSL/TLS handshakes.
- `server.jetty.accesslog.append` (Third party; type `Boolean`; default `false`): Append to log.
- `server.jetty.accesslog.custom-format` (Third party; type `String`): Custom log format, see org.eclipse.jetty.server.CustomRequestLog. If defined, overrides the "format" configuration key.
- `server.jetty.accesslog.date-format` (Third party; deprecation `ERROR`, replaced by `server.jetty.accesslog.custom-format`)
- `server.jetty.accesslog.enabled` (Third party; type `Boolean`; default `false`): Enable access log.
- `server.jetty.accesslog.extended-format` (Third party; deprecation `ERROR`, replaced by `server.jetty.accesslog.format`)
- `server.jetty.accesslog.file-date-format` (Third party; type `String`): Date format to place in log file name.
- `server.jetty.accesslog.filename` (Third party; type `String`): Log filename. If not specified, logs redirect to "System.err".
- `server.jetty.accesslog.format` (Third party; type `JettyServerProperties.Accesslog.Format`): Log format.
- `server.jetty.accesslog.ignore-paths` (Third party; type `List<String>`): Request paths that should not be logged.
- `server.jetty.accesslog.locale` (Third party; deprecation `ERROR`, replaced by `server.jetty.accesslog.custom-format`)
- `server.jetty.accesslog.log-cookies` (Third party; deprecation `ERROR`, replaced by `server.jetty.accesslog.custom-format`)
- `server.jetty.accesslog.log-latency` (Third party; deprecation `ERROR`, replaced by `server.jetty.accesslog.custom-format`)
- `server.jetty.accesslog.log-server` (Third party; deprecation `ERROR`, replaced by `server.jetty.accesslog.custom-format`)
- `server.jetty.accesslog.retention-period` (Third party; type `Integer`; default `31`): Number of days before rotated log files are deleted.
- `server.jetty.accesslog.time-zone` (Third party; deprecation `ERROR`, replaced by `server.jetty.accesslog.custom-format`)
- `server.jetty.connection-idle-timeout` (Third party; type `Duration`): Time that the connection can be idle before it is closed.
- `server.jetty.forwarded-headers.header-format` (Third party; type `JettyServerProperties.Forwardedheaders.HeaderFormat`; default `x-forwarded`): Format of the forwarded headers to support.
- `server.jetty.max-connections` (Third party; type `Integer`; default `-1`): Maximum number of connections that the server accepts and processes at any given time.
- `server.jetty.max-form-keys` (Third party; type `Integer`; default `1000`): Maximum number of form keys.
- `server.jetty.max-http-form-post-size` (Third party; type `DataSize`; default `200000B`): Maximum size of the form content in any HTTP post request.
- `server.jetty.max-http-post-size` (Third party; type `DataSize`; deprecation `ERROR`, replaced by `server.jetty.max-http-form-post-size`)
- `server.jetty.max-http-response-header-size` (Third party; type `DataSize`; default `16KB`): Maximum size of the HTTP response header.
- `server.jetty.threads.acceptors` (Third party; type `Integer`; default `-1`): Number of acceptor threads to use. When the value is -1, the default, the number of acceptors is derived from the operating environment.
- `server.jetty.threads.idle-timeout` (Third party; type `Duration`; default `60000ms`): Maximum thread idle time.
- `server.jetty.threads.max` (Third party; type `Integer`; default `200`): Maximum number of threads.
- `server.jetty.threads.max-queue-capacity` (Third party; type `Integer`): Maximum capacity of the thread pool's backing queue. A default is computed based on the threading configuration.
- `server.jetty.threads.min` (Third party; type `Integer`; default `8`): Minimum number of threads. Doesn't have an effect if virtual threads are enabled.
- `server.jetty.threads.selectors` (Third party; type `Integer`; default `-1`): Number of selector threads to use. When the value is -1, the default, the number of selectors is derived from the operating environment.
