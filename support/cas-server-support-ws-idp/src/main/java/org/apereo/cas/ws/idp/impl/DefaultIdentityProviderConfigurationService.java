package org.apereo.cas.ws.idp.impl;

import org.apereo.cas.ws.idp.IdentityProviderConfigurationService;
import org.apereo.cas.ws.idp.RealmAwareIdentityProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link DefaultIdentityProviderConfigurationService}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class DefaultIdentityProviderConfigurationService implements IdentityProviderConfigurationService {
    private List<RealmAwareIdentityProvider> identityProviders = new ArrayList<>();

    public DefaultIdentityProviderConfigurationService(final List<RealmAwareIdentityProvider> identityProviders) {
        this.identityProviders = identityProviders;
    }
    
    @Override
    public RealmAwareIdentityProvider getIdentityProvider(final String realm) {
        return this.identityProviders.stream().filter(i -> i.getRealm().equalsIgnoreCase(realm)).findFirst().orElse(null);
    }
}
