package org.apereo.cas.pac4j.client;

import org.pac4j.core.client.Client;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link DelegatedIdentityProviders}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public interface DelegatedIdentityProviders {

    /**
     * Default bean name.
     */
    String BEAN_NAME = "delegatedIdentityProviders";

    /**
     * Find all clients list.
     *
     * @return the list
     */
    List<Client> findAllClients();

    /**
     * Find client by its name.
     *
     * @param name the name
     * @return the optional
     */
    Optional<Client> findClient(String name);
}
