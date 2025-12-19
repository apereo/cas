package org.apereo.cas.pm.web.flow;

import module java.base;
import org.apereo.cas.pm.WeakPasswordException;
import org.apereo.cas.web.flow.authentication.CasWebflowExceptionHandler;
import lombok.Getter;
import lombok.Setter;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link WeakPasswordWebflowExceptionHandler}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Getter
@Setter
public class WeakPasswordWebflowExceptionHandler implements CasWebflowExceptionHandler<WeakPasswordException> {
    private int order;

    @Override
    public Event handle(final WeakPasswordException exception, final RequestContext requestContext) {
        return new Event(this, exception.getClass().getSimpleName(), new LocalAttributeMap<>("exception", exception));
    }

    @Override
    public boolean supports(final Exception exception, final RequestContext requestContext) {
        return exception instanceof WeakPasswordException;
    }
}
