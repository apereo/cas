package org.apereo.cas.support.pac4j.authentication;

import org.pac4j.core.client.Client;
import org.springframework.core.Ordered;

/**
 * This is {@link DelegatedClientFactoryCustomizer}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@FunctionalInterface
public interface DelegatedClientFactoryCustomizer<T extends Client> extends Ordered {
    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    /**
     * Customize the client.
     *
     * @param client the client
     */
    void customize(T client);
}
