package org.apereo.cas.configuration.model.core.web.session;

/**
 * This is {@link SessionStorageTypes}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public enum SessionStorageTypes {
    /**
     * Authentication requests, and other session data collected as part of authentication protocol flows and requests
     * are kept in the http servlet session that is local to the server.
     */
    HTTP,
    /**
     * Authentication requests, and other session data collected as part of authentication protocol flows and requests
     * are kept in the client browser's session storage, signed and encrypted. All interactions
     * require client-side read/write operations to restore the session from the browser.
     */
    BROWSER_STORAGE,
    /**
     * Authentication requests, and other session data collected as part of authentication protocol flows and requests
     * are tracked as CAS tickets in the registry and replicated across the entire cluster
     * as tickets. This state is tied to the user's agent/browser using a special cookie that would be used
     * to locate and restore that state. The cookie content may be signed and encrypted.
     * @deprecated Since 7.3.0; use {@link #HTTP} with session replication instead.
     */
    @Deprecated(since = "7.3.0", forRemoval = true)
    TICKET_REGISTRY;

    /**
     * Is ticket registry?.
     *
     * @return true/false
     * @deprecated Since 7.3.0; use {@link #HTTP} with session replication instead.
     */
    @Deprecated(since = "7.3.0", forRemoval = true)
    public boolean isTicketRegistry() {
        return this == TICKET_REGISTRY;
    }

    /**
     * Is browser storage?.
     *
     * @return true/false
     */
    public boolean isBrowserStorage() {
        return this == BROWSER_STORAGE;
    }
}
