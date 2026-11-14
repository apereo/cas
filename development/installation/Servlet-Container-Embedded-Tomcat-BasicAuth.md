# Apache Tomcat - Embedded Servlet Container Basic Authentication

Source: https://apereo.github.io/cas/development/installation/Servlet-Container-Embedded-Tomcat-BasicAuth.html

Protect CAS URLs with HTTP Basic authentication enforced by the embedded Apache Tomcat, using security roles and URL patterns.

The following settings and properties are available from the CAS configuration catalog:

- `cas.server.tomcat.basic-authn.enabled` (Required; type `Boolean`; default `false`): Enable Basic authentication for Tomcat.
- `cas.server.tomcat.basic-authn.auth-roles` (Optional; type `List<String>`): Add an authorization role, which is a role name that will be permitted access to the resources protected by this security constraint.
- `cas.server.tomcat.basic-authn.patterns` (Optional; type `List<String>`): Add a URL pattern to be part of this web resource collection.
- `cas.server.tomcat.basic-authn.security-roles` (Optional; type `List<String>`): Security roles for the CAS application.
