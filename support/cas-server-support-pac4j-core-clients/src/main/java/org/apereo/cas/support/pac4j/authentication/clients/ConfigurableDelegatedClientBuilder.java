package org.apereo.cas.support.pac4j.authentication.clients;

import java.util.List;

/**
 * This is {@link ConfigurableDelegatedClient}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@FunctionalInterface
public interface ConfigurableDelegatedClientBuilder {

    /**
     * Gets name.
     *
     * @return the name
     */
    default String getName() {
        return getClass().getSimpleName();
    }
    
    /**
     * Supply list of clients.
     *
     * @return the list
     * @throws Exception the exception
     */
    List<ConfigurableDelegatedClient> build() throws Exception;
}
