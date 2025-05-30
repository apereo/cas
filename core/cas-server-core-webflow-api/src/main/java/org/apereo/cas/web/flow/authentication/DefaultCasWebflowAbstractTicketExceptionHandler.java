package org.apereo.cas.web.flow.authentication;

import org.apereo.cas.ticket.AbstractTicketException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link DefaultCasWebflowAbstractTicketExceptionHandler}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Getter
@Setter
@RequiredArgsConstructor
public class DefaultCasWebflowAbstractTicketExceptionHandler implements CasWebflowExceptionHandler<AbstractTicketException> {
    /**
     * Ordered list of error classes that this class knows how to handle.
     */
    private final CasWebflowExceptionCatalog errors;

    private int order = Integer.MAX_VALUE - 1;

    @Override
    public Event handle(final AbstractTicketException exception, final RequestContext requestContext) {
        val id = handleAbstractTicketException(exception, requestContext);
        return EVENT_FACTORY.event(this, id);
    }

    @Override
    public boolean supports(final Exception exception, final RequestContext requestContext) {
        return exception instanceof AbstractTicketException;
    }

    /**
     * Maps an {@link AbstractTicketException} onto a state name equal to the simple class name of the exception with
     * highest precedence. Also sets an ERROR severity message in the message context with the error code found in
     * {@link AbstractTicketException#getCode()}. If no match is found,
     * {@value CasWebflowExceptionCatalog#UNKNOWN} is returned.
     *
     * @param e              Ticket exception to handle.
     * @param requestContext the spring context
     * @return Name of next flow state to transition to or {@value CasWebflowExceptionCatalog#UNKNOWN}
     */
    protected String handleAbstractTicketException(final AbstractTicketException e, final RequestContext requestContext) {
        return errors.translateException(requestContext, e);
    }
}
