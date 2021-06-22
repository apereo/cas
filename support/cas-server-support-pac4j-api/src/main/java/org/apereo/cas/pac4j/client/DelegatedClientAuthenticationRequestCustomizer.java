package org.apereo.cas.pac4j.client;

import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.context.WebContext;
import org.springframework.core.Ordered;

/**
 * This is {@link DelegatedClientAuthenticationRequestCustomizer}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public interface DelegatedClientAuthenticationRequestCustomizer extends Ordered {

    /**
     * Customize.
     *
     * @param client     the client
     * @param webContext the web context
     */
    void customize(IndirectClient client, WebContext webContext);

    /**
     * Supports.
     *
     * @param client     the client
     * @param webContext the web context
     * @return the boolean
     */
    boolean supports(IndirectClient client, WebContext webContext);

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
