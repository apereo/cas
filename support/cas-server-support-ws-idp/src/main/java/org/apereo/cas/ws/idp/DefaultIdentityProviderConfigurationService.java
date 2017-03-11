package org.apereo.cas.ws.idp;

import org.apereo.cas.ws.idp.api.FederationRelyingParty;
import org.apereo.cas.ws.idp.api.IdentityProviderConfigurationService;
import org.apereo.cas.ws.idp.api.RealmAwareIdentityProvider;

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
    private List<FederationRelyingParty> relyingParties = new ArrayList<>();

    public DefaultIdentityProviderConfigurationService(final List<RealmAwareIdentityProvider> identityProviders, 
                                                       final List<FederationRelyingParty> relyingParties) {
        this.identityProviders = identityProviders;
        this.relyingParties = relyingParties;
    }

    public List<RealmAwareIdentityProvider> getIdentityProviders() {
        return identityProviders;
    }

    public List<FederationRelyingParty> getRelyingParties() {
        return relyingParties;
    }

    @Override
    public RealmAwareIdentityProvider getIdentityProvider(final String realm) {
        return this.identityProviders.stream().filter(i -> i.getRealm().equalsIgnoreCase(realm)).findFirst().orElse(null);
    }

    @Override
    public FederationRelyingParty getRelyingParty(final String realm) {
        return this.relyingParties.stream().filter(i -> i.getRealm().equalsIgnoreCase(realm)).findFirst().orElse(null);
    }
}
