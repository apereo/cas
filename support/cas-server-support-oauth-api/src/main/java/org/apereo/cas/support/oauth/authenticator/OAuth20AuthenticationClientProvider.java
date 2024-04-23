package org.apereo.cas.support.oauth.authenticator;

import org.pac4j.core.client.Client;
import org.springframework.core.Ordered;

/**
 * This is {@link OAuth20AuthenticationClientProvider}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@FunctionalInterface
public interface OAuth20AuthenticationClientProvider extends Ordered {
    @Override
    default int getOrder() {
        return 0;
    }

    /**
     * Create client.
     *
     * @return the client
     */
    Client createClient();
}
