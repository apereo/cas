package org.apereo.cas.ws.idp;

/**
 * This is {@link IdentityProviderConfigurationService}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface IdentityProviderConfigurationService {
    /**
     * Gets identity provider.
     *
     * @param realm the realm
     * @return the identity provider
     */
    RealmAwareIdentityProvider getIdentityProvider(String realm);
}
