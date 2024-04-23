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
     */
    TICKET_REGISTRY
}
