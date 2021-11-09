package org.apereo.cas.support.pac4j.authentication;

import org.pac4j.core.client.Client;

import java.util.Collection;

/**
 * This is {@link DelegatedClientFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@FunctionalInterface
public interface DelegatedClientFactory<T extends Client> {
    /**
     * The bean name that identifies the saml2 message factory instance.
     */
    String BEAN_NAME_SAML2_CLIENT_MESSAGE_FACTORY = "delegatedSaml2ClientSAMLMessageStoreFactory";

    /**
     * Build set of clients configured.
     *
     * @return the set
     */
    Collection<T> build();
}
