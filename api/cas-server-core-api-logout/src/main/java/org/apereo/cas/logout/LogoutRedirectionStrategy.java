package org.apereo.cas.logout;

import org.springframework.core.Ordered;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link LogoutRedirectionStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public interface LogoutRedirectionStrategy extends Ordered {
    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    /**
     * Whether this strategy supports the given context.
     *
     * @param context the context
     * @return true/false
     */
    boolean supports(RequestContext context);

    /**
     * Handle redirects.
     *
     * @param context the context
     */
    void handle(RequestContext context);

    /**
     * Gets name.
     *
     * @return the name
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }
}
