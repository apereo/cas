# Apache Tomcat - Embedded Servlet Container Configuration

Source: https://apereo.github.io/cas/development/installation/Configuring-Servlet-Container-Embedded-Tomcat.html

Note that by default, the embedded container attempts to enable the HTTP2 protocol.

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
    implementation "org.apereo.cas:cas-server-webapp-tomcat"
}
```

## Configuration

The following settings and properties are available from the CAS configuration catalog:

- `server.tomcat.accept-count` (Third party; type `Integer`; default `100`): Maximum queue length for incoming connection requests when all possible request processing threads are in use.
- `server.tomcat.accesslog.buffered` (Third party; type `Boolean`; default `true`): Whether to buffer output such that it is flushed only periodically.
- `server.tomcat.accesslog.check-exists` (Third party; type `Boolean`; default `false`): Whether to check for log file existence so it can be recreated if an external process has renamed it.
- `server.tomcat.accesslog.condition-if` (Third party; type `String`): Whether logging of the request will only be enabled if "ServletRequest.getAttribute(conditionIf)" does not yield null.
- `server.tomcat.accesslog.condition-unless` (Third party; type `String`): Whether logging of the request will only be enabled if "ServletRequest.getAttribute(conditionUnless)" yield null.
- `server.tomcat.accesslog.directory` (Third party; type `String`; default `logs`): Directory in which log files are created. Can be absolute or relative to the Tomcat base dir.
- `server.tomcat.accesslog.enabled` (Third party; type `Boolean`; default `false`): Enable access log.
- `server.tomcat.accesslog.encoding` (Third party; type `String`): Character set used by the log file. Default to the system default character set.
- `server.tomcat.accesslog.file-date-format` (Third party; type `String`; default `.yyyy-MM-dd`): Date format to place in the log file name.
- `server.tomcat.accesslog.ipv6-canonical` (Third party; type `Boolean`; default `false`): Whether to use IPv6 canonical representation format as defined by RFC 5952.
- `server.tomcat.accesslog.locale` (Third party; type `String`): Locale used to format timestamps in log entries and in log file name suffix. Default to the default locale of the Java process.
- `server.tomcat.accesslog.max-days` (Third party; type `Integer`; default `-1`): Number of days to retain the access log files before they are removed.
- `server.tomcat.accesslog.pattern` (Third party; type `String`; default `common`): Format pattern for access logs.
- `server.tomcat.accesslog.prefix` (Third party; type `String`; default `access_log`): Log file name prefix.
- `server.tomcat.accesslog.rename-on-rotate` (Third party; type `Boolean`; default `false`): Whether to defer inclusion of the date stamp in the file name until rotate time.
- `server.tomcat.accesslog.request-attributes-enabled` (Third party; type `Boolean`; default `false`): Set request attributes for the IP address, Hostname, protocol, and port used for the request.
- `server.tomcat.accesslog.rotate` (Third party; type `Boolean`; default `true`): Whether to enable access log rotation.
- `server.tomcat.accesslog.suffix` (Third party; type `String`; default `.log`): Log file name suffix.
- `server.tomcat.additional-tld-skip-patterns` (Third party; type `List<String>`; deprecation `WARNING`, replaced by `server.tomcat.servlet.additional-tld-skip-patterns`)
- `server.tomcat.background-processor-delay` (Third party; type `Duration`; default `10s`): Delay between the invocation of backgroundProcess methods. If a duration suffix is not specified, seconds will be used.
- `server.tomcat.basedir` (Third party; type `File`): Tomcat base directory. If not specified, a temporary directory is used.
- `server.tomcat.connection-timeout` (Third party; type `Duration`): Amount of time the connector will wait, after accepting a connection, for the request URI line to be presented.
- `server.tomcat.keep-alive-timeout` (Third party; type `Duration`): Time to wait for another HTTP request before the connection is closed. When not set the connectionTimeout is used. When set to -1 there will be no timeout.
- `server.tomcat.max-connections` (Third party; type `Integer`; default `8192`): Maximum number of connections that the server accepts and processes at any given time. Once the limit has been reached, the operating system may still accept connections based on the "acceptCount" property.
- `server.tomcat.max-http-form-post-size` (Third party; type `DataSize`; default `2MB`): Maximum size of the form content in any HTTP post request.
- `server.tomcat.max-http-post-size` (Third party; type `DataSize`; deprecation `ERROR`, replaced by `server.tomcat.max-http-form-post-size`)
- `server.tomcat.max-http-response-header-size` (Third party; type `DataSize`; default `8KB`): Maximum size of the HTTP response header.
- `server.tomcat.max-keep-alive-requests` (Third party; type `Integer`; default `100`): Maximum number of HTTP requests that can be pipelined before the connection is closed. When set to 0 or 1, keep-alive and pipelining are disabled. When set to -1, an unlimited number of pipelined or keep-alive requests are allowed.
- `server.tomcat.max-locale-count` (Third party; type `Integer`; default `10`): Maximum number of locales that will be parsed from the Accept-Language header of a request. A value of less than 0 means no limit.
- `server.tomcat.max-parameter-count` (Third party; type `Integer`; default `1000`): Maximum number of parameters (GET plus POST) that will be automatically parsed by the container. A value of less than 0 means no limit.
- `server.tomcat.max-part-count` (Third party; type `Integer`; default `50`): Maximum total number of parts permitted in a multipart/form-data request. Requests that exceed this limit will be rejected. A value of less than 0 means no limit.
- `server.tomcat.max-part-header-size` (Third party; type `DataSize`; default `512B`): Maximum per-part header size permitted in a multipart/form-data request. Requests that exceed this limit will be rejected. A value of less than 0 means no limit.
- `server.tomcat.max-swallow-size` (Third party; type `DataSize`; default `2MB`): Maximum amount of request body to swallow.
- `server.tomcat.mbeanregistry.enabled` (Third party; type `Boolean`; default `false`): Whether Tomcat's MBean Registry should be enabled.
- `server.tomcat.processor-cache` (Third party; type `Integer`; default `200`): Maximum number of idle processors that will be retained in the cache and reused with a subsequent request. When set to -1 the cache will be unlimited with a theoretical maximum size equal to the maximum number of connections.
- `server.tomcat.redirect-context-root` (Third party; type `Boolean`; deprecation `WARNING`, replaced by `server.tomcat.servlet.redirect-context-root`)
- `server.tomcat.reject-illegal-header` (Third party; deprecation `ERROR`, no replacement)
- `server.tomcat.relaxed-path-chars` (Third party; type `List<Character>`): List of additional unencoded characters that should be allowed in URI paths. Only "< > [ \ ] ^ ` { | }" are allowed.
- `server.tomcat.relaxed-query-chars` (Third party; type `List<Character>`): List of additional unencoded characters that should be allowed in URI query strings. Only "< > [ \ ] ^ ` { | }" are allowed.
- `server.tomcat.remoteip.host-header` (Third party; type `String`; default `X-Forwarded-Host`): Name of the HTTP header from which the remote host is extracted.
- `server.tomcat.remoteip.internal-proxies` (Third party; type `String`; default `10.0.0.0/8, 192.168.0.0/16, 169.254.0.0/16, 100.64.0.0/10, fc00::/7, 172.16.0.0/12, ::1/128, 127.0.0.0/8, fe80::/10`): Internal proxies that are to be trusted. Can be set as a comma separate list of CIDR or as a regular expression.
- `server.tomcat.remoteip.port-header` (Third party; type `String`; default `X-Forwarded-Port`): Name of the HTTP header used to override the original port value.
- `server.tomcat.remoteip.protocol-header` (Third party; type `String`): Header that holds the incoming protocol, usually named "X-Forwarded-Proto".
- `server.tomcat.remoteip.protocol-header-https-value` (Third party; type `String`; default `https`): Value of the protocol header indicating whether the incoming request uses SSL.
- `server.tomcat.remoteip.remote-ip-header` (Third party; type `String`): Name of the HTTP header from which the remote IP is extracted. For instance, 'X-FORWARDED-FOR'.
- `server.tomcat.remoteip.trusted-proxies` (Third party; type `String`): Regular expression defining proxies that are trusted when they appear in the "remote-ip-header" header.
- `server.tomcat.resource.allow-caching` (Third party; type `Boolean`; default `true`): Whether static resource caching is permitted for this web application.
- `server.tomcat.resource.cache-max-size` (Third party; type `DataSize`; default `10MB`): Maximum size of the static resource cache.
- `server.tomcat.resource.cache-ttl` (Third party; type `Duration`; default `5s`): Time-to-live of the static resource cache.
- `server.tomcat.servlet.additional-tld-skip-patterns` (Third party; type `List<String>`): List of additional patterns that match jars to ignore for TLD scanning. The special '?' and '*' characters can be used in the pattern to match one and only one character and zero or more characters respectively.
- `server.tomcat.servlet.redirect-context-root` (Third party; type `Boolean`; default `true`): Whether requests to the context root should be redirected by appending a / to the path. When using SSL terminated at a proxy, this property should be set to false.
- `server.tomcat.servlet.use-relative-redirects` (Third party; type `Boolean`; default `true`): Whether HTTP 1.1 and later location headers generated by a call to sendRedirect will use relative or absolute redirects.
- `server.tomcat.threads.max` (Third party; type `Integer`; default `200`): Maximum amount of worker threads. Doesn't have an effect if virtual threads are enabled.
- `server.tomcat.threads.max-queue-capacity` (Third party; type `Integer`; default `2147483647`): Maximum capacity of the thread pool's backing queue. This setting only has an effect if the value is greater than 0.
- `server.tomcat.threads.min-spare` (Third party; type `Integer`; default `10`): Minimum amount of worker threads. Doesn't have an effect if virtual threads are enabled.
- `server.tomcat.uri-encoding` (Third party; type `Charset`; default `UTF-8`): Character encoding to use to decode the URI.
- `server.tomcat.use-apr` (Third party; type `TomcatServerProperties.UseApr`; default `never`): Whether to use APR.
- `server.tomcat.use-relative-redirects` (Third party; type `Boolean`; deprecation `WARNING`, replaced by `server.tomcat.servlet.use-relative-redirects`)

## IPv4 Configuration

In order to force Apache Tomcat to use IPv4, configure the following as a system property for your *run* command:

```bash
-Djava.net.preferIPv4Stack=true
```

The same sort of configuration needs to be applied to your `$CATALINA_OPTS` environment variable in case of an external container.
