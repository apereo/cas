package org.apereo.cas.support.pac4j.authentication.clients;

import org.pac4j.core.client.IndirectClient;

import java.util.Collection;

/**
 * This is {@link DelegatedClientFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public interface DelegatedClientFactory {
    /**
     * The bean name that identifies the saml2 message factory instance.
     */
    String BEAN_NAME_SAML2_CLIENT_MESSAGE_FACTORY = "delegatedSaml2ClientSAMLMessageStoreFactory";

    /**
     * Build set of clients configured.
     *
     * @return the set
     */
    Collection<IndirectClient> build();

    /**
     * Rebuild collection and invalidate the cached entries, if any.
     *
     * @return the collection
     */
    Collection<IndirectClient> rebuild();
}
