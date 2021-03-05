package org.apereo.cas.web.flow.authentication;

import org.apereo.cas.ticket.AbstractTicketException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Set;

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
    private int order = Integer.MAX_VALUE - 1;

    /**
     * Ordered list of error classes that this class knows how to handle.
     */
    private final Set<Class<? extends Throwable>> errors;

    /**
     * String appended to exception class name to create a message bundle key for that particular error.
     */
    private final String messageBundlePrefix;

    @Override
    public Event handle(final AbstractTicketException exception, final RequestContext requestContext) {
        val id = handleAbstractTicketException(exception, requestContext);
        return new EventFactorySupport().event(this, id);
    }

    @Override
    public boolean supports(final Exception exception, final RequestContext requestContext) {
        return exception instanceof AbstractTicketException;
    }

    /**
     * Maps an {@link AbstractTicketException} onto a state name equal to the simple class name of the exception with
     * highest precedence. Also sets an ERROR severity message in the message context with the error code found in
     * {@link AbstractTicketException#getCode()}. If no match is found,
     * {@value CasWebflowExceptionHandler#UNKNOWN} is returned.
     *
     * @param e              Ticket exception to handle.
     * @param requestContext the spring context
     * @return Name of next flow state to transition to or {@value CasWebflowExceptionHandler#UNKNOWN}
     */
    protected String handleAbstractTicketException(final AbstractTicketException e, final RequestContext requestContext) {
        val messageContext = requestContext.getMessageContext();
        val match = this.errors.stream()
            .filter(c -> c.isInstance(e)).map(Class::getSimpleName)
            .findFirst();

        val msg = new MessageBuilder()
            .error()
            .code(e.getCode())
            .args(e.getArgs().toArray())
            .build();
        match.ifPresent(s -> messageContext.addMessage(msg));
        return match.orElse(CasWebflowExceptionHandler.UNKNOWN);
    }
}
