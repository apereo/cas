package org.apereo.cas.pac4j.client;

import module java.base;
import org.apereo.cas.authentication.principal.Service;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.pac4j.core.client.Client;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.util.InitializableObject;

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
     * Initialize the client, waiting for an initialization already in progress on another thread.
     *
     * @param <T>    the client type
     * @param client the client
     * @return the client
     */
    static <T extends InitializableObject> T initialize(final T client) {
        client.init();
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
        while (!client.isInitialized() && client.isInitializing() && System.nanoTime() < deadline) {
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(10));
        }
        if (!client.isInitialized()) {
            client.init();
        }
        return client;
    }

    /**
     * Find all clients by service.
     *
     * @param service    the service
     * @param webContext the web context
     * @return the list
     */
    List<? extends Client> findAllClients(@Nullable Service service, WebContext webContext);

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
