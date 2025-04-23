package org.apereo.cas.web.flow.resolver.impl;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.resolver.CasWebflowAware;
import lombok.experimental.UtilityClass;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import java.util.List;

/**
 * This is {@link WebflowExceptionTranslator}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@UtilityClass
class WebflowExceptionTranslator {
    private static final EventFactorySupport EVENT_FACTORY_SUPPORT = new EventFactorySupport();

    private static final List<Class> SUPPORTED_EXCEPTIONS = List.of(
        AuthenticationException.class,
        AbstractTicketException.class,
        CasWebflowAware.class);

    public static Event from(final Throwable exception, final RequestContext requestContext) {
        if (isSupportedException(exception)) {
            return EVENT_FACTORY_SUPPORT.event(exception, CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE);
        }
        if (isSupportedException(exception.getCause())) {
            return EVENT_FACTORY_SUPPORT.event(exception.getCause(), CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE);
        }
        return EVENT_FACTORY_SUPPORT.event(exception, CasWebflowConstants.TRANSITION_ID_ERROR);
    }

    private static boolean isSupportedException(final Throwable exception) {
        return exception != null && SUPPORTED_EXCEPTIONS.stream().anyMatch(type -> type.isAssignableFrom(exception.getClass()));
    }
}
