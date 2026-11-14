# Apache Tomcat - Embedded Servlet Container Access Log

Source: https://apereo.github.io/cas/development/installation/Servlet-Container-Embedded-Tomcat-ExtAccessLog.html

Write an extended access log from the embedded Apache Tomcat, with a configurable pattern, file name and directory.

The following settings and properties are available from the CAS configuration catalog:

- `cas.server.tomcat.ext-access-log.enabled` (Required; type `Boolean`; default `false`): Flag to indicate whether extended log facility is enabled.
- `cas.server.tomcat.ext-access-log.directory` (Optional; type `String`): Directory name for extended log.
- `cas.server.tomcat.ext-access-log.pattern` (Optional; type `String`; default `c-ip s-ip cs-uri sc-status time x-threadname x-H(secure) x-H(remoteUser)`): String representing extended log pattern.
- `cas.server.tomcat.ext-access-log.prefix` (Optional; type `String`; default `localhost_access_extended`): File name prefix for extended log.
- `cas.server.tomcat.ext-access-log.suffix` (Optional; type `String`; default `.log`): File name suffix for extended log.
