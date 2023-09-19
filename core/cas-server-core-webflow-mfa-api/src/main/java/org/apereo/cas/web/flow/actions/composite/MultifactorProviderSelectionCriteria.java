package org.apereo.cas.web.flow.actions.composite;

import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link MultifactorProviderSelectionCriteria}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@FunctionalInterface
public interface MultifactorProviderSelectionCriteria {
    /**
     * Should proceed with multifactor provider selection.
     *
     * @param requestContext the request context
     * @return true/false
     */
    boolean shouldProceedWithMultifactorProviderSelection(RequestContext requestContext);

    /**
     * Proceed multifactor provider selection criteria.
     *
     * @return the multifactor provider selection criteria
     */
    static MultifactorProviderSelectionCriteria select() {
        return requestContext -> false;
    }
}
