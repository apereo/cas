package org.apereo.cas.pac4j.client;

import module java.base;
import org.apereo.cas.authentication.principal.WebApplicationService;
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
     * @throws Throwable the throwable
     */
    void customize(IndirectClient client, WebContext webContext) throws Throwable;

    /**
     * Supports.
     *
     * @param client     the client
     * @param webContext the web context
     * @return true /false
     * @throws Throwable the throwable
     */
    boolean supports(IndirectClient client, WebContext webContext) throws Throwable;

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    /**
     * Is client authorized for use in this context.
     *
     * @param webContext     the web context
     * @param client         the client
     * @param currentService the current service
     * @return true /false
     * @throws Throwable the throwable
     */
    boolean isAuthorized(WebContext webContext, IndirectClient client, WebApplicationService currentService) throws Throwable;
}
