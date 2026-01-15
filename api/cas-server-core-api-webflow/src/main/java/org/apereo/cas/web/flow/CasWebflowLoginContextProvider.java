package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.util.NamedObject;
import org.springframework.core.Ordered;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link CasWebflowLoginContextProvider}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public interface CasWebflowLoginContextProvider extends Ordered, NamedObject {

    /**
     * Gets candidate username.
     *
     * @param requestContext the request context
     * @return the candidate username
     */
    default Optional<String> getCandidateUsername(final RequestContext requestContext) {
        return Optional.empty();
    }

    /**
     * Is login form username input disabled?
     *
     * @param requestContext the request context
     * @return true/false
     */
    default boolean isLoginFormUsernameInputDisabled(final RequestContext requestContext) {
        return false;
    }

    /**
     * Is login form username input visible?
     *
     * @param requestContext the request context
     * @return true/false
     */
    default boolean isLoginFormUsernameInputVisible(final RequestContext requestContext) {
        return false;
    }

    /**
     * Is login form viewable?.
     *
     * @param requestContext the request context
     * @return true or false
     */
    default boolean isLoginFormViewable(final RequestContext requestContext) {
        return false;
    }

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
