# Apache Tomcat - Embedded Servlet Container Rewrite Valve

Source: https://apereo.github.io/cas/development/installation/Servlet-Container-Embedded-Tomcat-RewriteValve.html

The rewrite valve rewrites request URLs using `mod_rewrite`-style rules before they reach CAS.

The following settings and properties are available from the CAS configuration catalog:

- `cas.server.tomcat.rewrite-valve.location` (Required; type `Resource`): Location of a rewrite valve specifically by Apache Tomcat to activate URL rewriting.
- `cas.server.tomcat.rewrite-valve.valve-type` (Optional; type `CasEmbeddedApacheTomcatValveTypes`; default `context`): Define how the valve would be added and configured for Apache Tomcat. Available values are as follows: - `CONTEXT`: Context valves live on a specific web‐application’s `Context` container (type `StandardContext`) and only see requests once they’ve been routed to that particular webapp. - `ENGINE`: Engine valves live on the `Engine` container (type `StandardEngine`) and are invoked for every request, before Tomcat even selects a virtual‐host.
