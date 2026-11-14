# Apache Tomcat - Embedded Servlet Container Remote User

Source: https://apereo.github.io/cas/development/installation/Servlet-Container-Embedded-Tomcat-RemoteUserValve.html

Accept the authenticated user name from an HTTP header set by a trusted proxy, optionally only from allowed IP addresses, using a Tomcat remote user valve.

The following settings and properties are available from the CAS configuration catalog:

- `cas.server.tomcat.remote-user-valve.remote-user-header` (Required; type `String`): The name of the remote-user header that should be passed onto the http servlet request. Leaving this setting as blank or undefined will deactivate the valve altogether. The header is typically passed down to tomcat via proxies, load balancers, etc.
- `cas.server.tomcat.remote-user-valve.allowed-ip-address-regex` (Optional; type `String`; default `.+`): A regular expression (using java.util.regex) that the remote client's IP address is compared to. If this attribute is specified, the remote address MUST match for this request to be accepted. If this attribute is not specified, all requests will be accepted.
