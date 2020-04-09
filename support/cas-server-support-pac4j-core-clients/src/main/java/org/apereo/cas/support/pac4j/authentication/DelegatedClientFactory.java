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
     * Build set of clients configured.
     *
     * @return the set
     */
    Collection<T> build();
}
