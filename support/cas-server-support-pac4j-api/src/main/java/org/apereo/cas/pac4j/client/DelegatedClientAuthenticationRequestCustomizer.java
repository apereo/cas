package org.apereo.cas.pac4j.client;

import module java.base;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.context.WebContext;
import org.springframework.core.Ordered;
import org.springframework.webflow.execution.RequestContext;

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
     * @param client         the client
     * @param webContext     the web context
     * @param requestContext the request context
     * @throws Throwable the throwable
     */
    void customize(IndirectClient client, WebContext webContext, RequestContext requestContext) throws Throwable;

    /**
     * Supports.
     *
     * @param client         the client
     * @param webContext     the web context
     * @param requestContext the request context
     * @return true /false
     * @throws Throwable the throwable
     */
    boolean supports(IndirectClient client, WebContext webContext, RequestContext requestContext) throws Throwable;

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
     * @param requestContext the request context
     * @return true /false
     * @throws Throwable the throwable
     */
    default boolean isAuthorized(final WebContext webContext,
                                 final IndirectClient client,
                                 final WebApplicationService currentService,
                                 final RequestContext requestContext) throws Throwable {
        return true;
    }
}
