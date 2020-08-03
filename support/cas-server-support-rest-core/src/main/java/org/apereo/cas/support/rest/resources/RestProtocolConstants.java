package org.apereo.cas.support.rest.resources;

/**
 * This is {@link RestProtocolConstants}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public interface RestProtocolConstants {

    /**
     * The base endpoint for REST protocol.
     */
    String BASE_ENDPOINT = "/v1";
    
    /**
     * Tickets endpoint.
     */
    String ENDPOINT_TICKETS = BASE_ENDPOINT + "/tickets";

    /**
     * Users endpoint.
     */
    String ENDPOINT_USERS = BASE_ENDPOINT + "/users";
}
