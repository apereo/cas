# Apache Tomcat - Embedded Servlet Container Session Initialization

Source: https://apereo.github.io/cas/development/installation/Servlet-Container-Embedded-Tomcat-SessionInitialization.html

Turn on the Tomcat filter that creates the HTTP session at the start of every request, so it exists before later processing needs it.

The following settings and properties are available from the CAS configuration catalog:

- `cas.server.tomcat.session-initialization.enabled` (Required; type `Boolean`; default `false`): Enable filter.
