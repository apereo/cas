package org.apereo.cas.web.flow.resolver.impl;

import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Set;

/**
 * This is {@link NoOpCasWebflowEventResolver} that does not resolve any events.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class NoOpCasWebflowEventResolver extends AbstractCasWebflowEventResolver {
    @Override
    public Set<Event> resolveInternal(final RequestContext context) {
        return null;
    }
}
