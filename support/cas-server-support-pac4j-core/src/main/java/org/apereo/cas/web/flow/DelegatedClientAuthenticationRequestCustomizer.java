package org.apereo.cas.web.flow;

import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.context.WebContext;
import org.springframework.core.Ordered;

/**
 * This is {@link DelegatedClientAuthenticationRequestCustomizer}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@FunctionalInterface
public interface DelegatedClientAuthenticationRequestCustomizer extends Ordered {

    /**
     * Customize.
     *
     * @param client     the client
     * @param webContext the web context
     */
    void customize(IndirectClient client, WebContext webContext);

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
