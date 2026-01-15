package org.apereo.cas.support.pac4j.authentication.clients;

import module java.base;
import org.pac4j.core.client.BaseClient;

/**
 * This is {@link DelegatedClientsEndpointContributor}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public interface DelegatedClientsEndpointContributor {
    /**
     * Supports this client?
     *
     * @param client the client
     * @return true/false
     */
    boolean supports(BaseClient client);

    /**
     * Contribute map.
     *
     * @param client the client
     * @return the map
     */
    Map<String, Object> contribute(BaseClient client);
}
