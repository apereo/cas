package org.apereo.cas.support.pac4j;

import org.apereo.cas.support.pac4j.authentication.DelegatedClientFactory;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link RefreshableDelegatedClients}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Slf4j
public class RefreshableDelegatedClients extends Clients {
    private final DelegatedClientFactory<Client> delegatedClientFactory;

    public RefreshableDelegatedClients(final String callbackUrl,
                                       final DelegatedClientFactory<Client> delegatedClientFactory) {
        setCallbackUrl(callbackUrl);
        this.delegatedClientFactory = delegatedClientFactory;
    }

    @Override
    public Optional<Client> findClient(final String name) {
        setClients(buildDelegatedClients());
        init();
        return super.findClient(name);
    }

    @Override
    public List<Client> findAllClients() {
        setClients(buildDelegatedClients());
        init();
        return super.findAllClients();
    }

    /**
     * Build delegated clients.
     *
     * @return the list
     */
    protected List<Client> buildDelegatedClients() {
        val clients = delegatedClientFactory.build();
        LOGGER.debug("The following clients are built: [{}]", clients);
        return new ArrayList<>(clients);
    }
}
