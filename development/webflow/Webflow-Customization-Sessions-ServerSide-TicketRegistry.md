# Server-side Sessions - Ticket Registry

Source: https://apereo.github.io/cas/development/webflow/Webflow-Customization-Sessions-ServerSide-TicketRegistry.html

If you don’t wish to use the native container’s strategy for session replication, and the session storage backend of your choice is not natively supported by Spring Session, you can use CAS’ support for the [Ticket Registry](https://apereo.github.io/cas/development/ticketing/Configuring-Ticketing-Components.html) to store sessions.

This option translates session data and attributes into a `TST` ticket, and stores it in the configured ticket registry. Note that by *Session*, we mean the HTTP session that is created by the container such as Apache Tomcat specific to the runtime environment, and is represented by the `jakarta.servlet.http.HttpSession` object.

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-session-ticket-registry"
}
```
