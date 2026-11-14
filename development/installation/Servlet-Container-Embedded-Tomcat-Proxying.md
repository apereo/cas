# Apache Tomcat - Embedded Servlet Container Proxying

Source: https://apereo.github.io/cas/development/installation/Servlet-Container-Embedded-Tomcat-Proxying.html

Open an additional plain HTTP connector on the embedded Apache Tomcat, for example when a proxy or load balancer terminates TLS and forwards requests to CAS over HTTP.

The following settings and properties are available from the CAS configuration catalog:

- `cas.server.tomcat.http-proxy.enabled` (Required; type `Boolean`; default `false`): Enable the container running in proxy mode.
- `cas.server.tomcat.http[0].enabled` (Required; type `boolean`): Enable a separate port for the embedded container for HTTP access.
- `cas.server.tomcat.http` (Optional; type `List<CasEmbeddedApacheTomcatHttpProperties>`): Embedded container HTTP port settings as an additional option.
- `cas.server.tomcat.http-proxy.attributes` (Optional; type `Map<String,String>`): Custom attributes to set on the proxy connector.
- `cas.server.tomcat.http-proxy.protocol` (Optional; type `String`; default `AJP/1.3`): Proxy protocol to use.
- `cas.server.tomcat.http-proxy.proxy-port` (Optional; type `Integer`; default `0`): Proxy port for the proxy.
- `cas.server.tomcat.http-proxy.redirect-port` (Optional; type `Integer`; default `0`): Redirect port for the proxy.
- `cas.server.tomcat.http-proxy.scheme` (Optional; type `String`; default `https`): Scheme used for the proxy.
- `cas.server.tomcat.http-proxy.secret` (Optional; type `String`): Set the secret that must be included with every request.
- `cas.server.tomcat.http-proxy.secure` (Optional; type `Boolean`; default `true`): Whether proxy should run in secure mode.
- `cas.server.tomcat.http[0].attributes` (Optional; type `Map<String, String>`): Additional attributes to be set on the connector.
- `cas.server.tomcat.http[0].port` (Optional; type `int`; default `8080`): The HTTP port to use.
- `cas.server.tomcat.http[0].protocol` (Optional; type `String`; default `org.apache.coyote.http11.Http11NioProtocol`): HTTP protocol to use.
- `cas.server.tomcat.http[0].redirect-port` (Optional; type `int`): If this Connector is supporting non-SSL requests, this will automatically redirect the request to the port number specified here. Matching security constraints that require SSL transport will be auto-defined.
- `cas.server.tomcat.http[0].scheme` (Optional; type `String`; default `http`): Scheme used for the connector.
- `cas.server.tomcat.http[0].secure` (Optional; type `boolean`): Whether connector should run in secure mode.
