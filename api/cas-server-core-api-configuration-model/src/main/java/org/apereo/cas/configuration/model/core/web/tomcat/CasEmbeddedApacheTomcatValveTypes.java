package org.apereo.cas.configuration.model.core.web.tomcat;

/**
 * This is {@link CasEmbeddedApacheTomcatValveTypes}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
public enum CasEmbeddedApacheTomcatValveTypes {
    /**
     * Context valves live on a specific web‐application’s {@code Context} container
     * (type {@code StandardContext}) and only see requests once they’ve been routed to that particular webapp.
     */
    CONTEXT,

    /**
     * Engine valves live on the {@code Engine} container (type {@code StandardEngine})
     * and are invoked for every request, before Tomcat even selects a virtual‐host.
     */
    ENGINE
}
