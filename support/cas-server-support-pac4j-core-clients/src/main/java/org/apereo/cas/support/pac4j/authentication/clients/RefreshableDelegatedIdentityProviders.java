package org.apereo.cas.support.pac4j.authentication.clients;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviderFactory;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviders;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.WebContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link RefreshableDelegatedIdentityProviders}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Slf4j
public class RefreshableDelegatedIdentityProviders extends Clients implements DelegatedIdentityProviders {
    protected final DelegatedIdentityProviderFactory delegatedIdentityProviderFactory;

    public RefreshableDelegatedIdentityProviders(final String callbackUrl,
                                                 final DelegatedIdentityProviderFactory delegatedIdentityProviderFactory) {
        setCallbackUrl(callbackUrl);
        this.delegatedIdentityProviderFactory = delegatedIdentityProviderFactory;
    }

    @Override
    public Optional<Client> findClient(final String name) {
        setClients(buildDelegatedClients());
        init();
        return super.findClient(name);
    }

    @Override
    public List<Client> findAllClients(final Service service, final WebContext webContext) {
        return findAllClients();
    }

    @Override
    public List<Client> findAllClients() {
        setClients(buildDelegatedClients());
        init();
        return super.findAllClients();
    }

    protected List<Client> buildDelegatedClients() {
        val providers = delegatedIdentityProviderFactory.build();
        LOGGER.trace("The following clients are built: [{}]", providers);
        return new ArrayList<>(providers);
    }
}
