package org.apereo.cas.pac4j.client;

import org.pac4j.core.client.BaseClient;
import java.util.Collection;
import java.util.List;

/**
 * This is {@link DelegatedIdentityProviderFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public interface DelegatedIdentityProviderFactory {
    /**
     * The bean name that identifies the saml2 message factory instance.
     */
    String BEAN_NAME_SAML2_CLIENT_MESSAGE_FACTORY = "delegatedSaml2ClientSAMLMessageStoreFactory";

    /**
     * Build set of clients configured.
     *
     * @return the set
     */
    Collection<BaseClient> build();

    /**
     * Rebuild collection and invalidate the cached entries, if any.
     *
     * @return the collection
     */
    Collection<BaseClient> rebuild();

    /**
     * Factory that produces static list of clients.
     *
     * @param clients the clients
     * @return the delegated client factory
     */
    static DelegatedIdentityProviderFactory withClients(final List clients) {
        return new DelegatedIdentityProviderFactory() {

            @Override
            public Collection<BaseClient> build() {
                return clients;
            }

            @Override
            public Collection<BaseClient> rebuild() {
                return clients;
            }
        };
    }
}
