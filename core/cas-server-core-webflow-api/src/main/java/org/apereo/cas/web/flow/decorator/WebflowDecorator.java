package org.apereo.cas.web.flow.decorator;

import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link WebflowDecorator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@FunctionalInterface
public interface WebflowDecorator {
    /**
     * Decorate the request context in the webflow with additional info
     * and manipulate relevant scopes. Note that objects put into the webflow
     * as the result of decorations MUST be serializable.
     *
     * @param requestContext the context
     * @throws Throwable the throwable
     */
    void decorate(RequestContext requestContext) throws Throwable;

    /**
     * No op webflow decorator.
     *
     * @return the webflow decorator
     */
    static WebflowDecorator noOp() {
        return requestContext -> {
        };
    }
}
