package org.apereo.cas.pac4j.client;

import org.apereo.cas.authentication.principal.Service;
import org.pac4j.core.client.Client;
import org.pac4j.core.context.WebContext;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link DelegatedIdentityProviders}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@FunctionalInterface
public interface DelegatedIdentityProviders {

    /**
     * Default bean name.
     */
    String BEAN_NAME = "delegatedIdentityProviders";

    /**
     * Find all clients by service.
     *
     * @param service    the service
     * @param webContext the web context
     * @return the list
     */
    List<? extends Client> findAllClients(Service service, WebContext webContext);

    /**
     * Find all clients list.
     *
     * @param webContext the web context
     * @return the list
     */
    default List<? extends Client> findAllClients(final WebContext webContext) {
        return findAllClients(null, webContext);
    }

    /**
     * Find client by its name.
     *
     * @param name       the name
     * @param webContext the web context
     * @return the optional
     */
    default Optional<? extends Client> findClient(final String name, final WebContext webContext) {
        return findAllClients(webContext).stream()
            .filter(client -> client.getName().equalsIgnoreCase(name))
            .findFirst();
    }
}
