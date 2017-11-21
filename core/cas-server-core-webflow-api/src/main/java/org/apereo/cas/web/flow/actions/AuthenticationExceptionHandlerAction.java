package org.apereo.cas.web.flow.actions;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.services.UnauthorizedServiceForPrincipalException;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Performs two important error handling functions on an
 * {@link org.apereo.cas.authentication.AuthenticationException} raised from the authentication
 * layer:
 * <ol>
 * <li>Maps handler errors onto message bundle strings for display to user.</li>
 * <li>Determines the next webflow state by comparing handler errors against {@link #errors}
 * in list order. The first entry that matches determines the outcome state, which
 * is the simple class name of the exception.</li>
 * </ol>
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class AuthenticationExceptionHandlerAction extends AbstractAction {

    private static final String DEFAULT_MESSAGE_BUNDLE_PREFIX = "authenticationFailure.";
    private static final String UNKNOWN = "UNKNOWN";

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationExceptionHandlerAction.class);

    /**
     * Ordered list of error classes that this class knows how to handle.
     */
    private final Set<Class<? extends Exception>> errors;

    /**
     * String appended to exception class name to create a message bundle key for that particular error.
     */
    private String messageBundlePrefix = DEFAULT_MESSAGE_BUNDLE_PREFIX;

    public AuthenticationExceptionHandlerAction() {
        this(new LinkedHashSet<>());
    }

    public AuthenticationExceptionHandlerAction(final Set<Class<? extends Exception>> errors) {
        this.errors = errors;
    }

    public Set<Class<? extends Exception>> getErrors() {
        return new LinkedHashSet<>(this.errors);
    }

    /**
     * Maps an authentication exception onto a state name.
     * Also sets an ERROR severity message in the message context.
     *
     * @param e              Authentication error to handle.
     * @param requestContext the spring  context
     * @return Name of next flow state to transition to or {@value #UNKNOWN}
     */
    public String handle(final Exception e, final RequestContext requestContext) {
        final MessageContext messageContext = requestContext.getMessageContext();

        if (e instanceof AuthenticationException) {
            return handleAuthenticationException((AuthenticationException) e, requestContext);
        }

        if (e instanceof AbstractTicketException) {
            return handleAbstractTicketException((AbstractTicketException) e, requestContext);
        }

        LOGGER.trace("Unable to translate errors of the authentication exception [{}]. Returning [{}]", e, UNKNOWN);
        final String messageCode = this.messageBundlePrefix + UNKNOWN;
        messageContext.addMessage(new MessageBuilder().error().code(messageCode).build());
        return UNKNOWN;
    }

    /**
     * Maps an authentication exception onto a state name equal to the simple class name of the {@link
     * AuthenticationException#getHandlerErrors()}
     * with highest precedence. Also sets an ERROR severity message in the
     * message context of the form {@code [messageBundlePrefix][exceptionClassSimpleName]}
     * for for the first handler
     * error that is configured. If no match is found, {@value #UNKNOWN} is returned.
     *
     * @param e              Authentication error to handle.
     * @param requestContext the spring context
     * @return Name of next flow state to transition to or {@value #UNKNOWN}
     */
    protected String handleAuthenticationException(final AuthenticationException e,
                                                   final RequestContext requestContext) {

        if (e.getHandlerErrors().containsKey(UnauthorizedServiceForPrincipalException.class.getSimpleName())) {
            final URI url = WebUtils.getUnauthorizedRedirectUrlIntoFlowScope(requestContext);
            if (url != null) {
                LOGGER.warn("Unauthorized service access for principal; CAS will be redirecting to [{}]", url);
                return CasWebflowConstants.STATE_ID_SERVICE_UNAUTHZ_CHECK;
            }
        }

        final String handlerErrorName = this.errors
                .stream()
                .filter(e.getHandlerErrors().values()::contains)
                .map(Class::getSimpleName)
                .findFirst()
                .orElseGet(() -> {
                    LOGGER.debug("Unable to translate handler errors of the authentication exception [{}]. Returning [{}]", e, UNKNOWN);
                    return UNKNOWN;
                });

        final MessageContext messageContext = requestContext.getMessageContext();
        final String messageCode = this.messageBundlePrefix + handlerErrorName;
        messageContext.addMessage(new MessageBuilder().error().code(messageCode).build());
        return handlerErrorName;
    }

    /**
     * Maps an {@link AbstractTicketException} onto a state name equal to the simple class name of the exception with
     * highest precedence. Also sets an ERROR severity message in the message context with the error code found in
     * {@link AbstractTicketException#getCode()}. If no match is found,
     * {@value #UNKNOWN} is returned.
     *
     * @param e              Ticket exception to handle.
     * @param requestContext the spring context
     * @return Name of next flow state to transition to or {@value #UNKNOWN}
     */
    protected String handleAbstractTicketException(final AbstractTicketException e, final RequestContext requestContext) {
        final MessageContext messageContext = requestContext.getMessageContext();
        final Optional<String> match = this.errors.stream()
                .filter(c -> c.isInstance(e)).map(Class::getSimpleName)
                .findFirst();

        match.ifPresent(s -> messageContext.addMessage(new MessageBuilder().error().code(e.getCode()).build()));
        return match.orElse(UNKNOWN);
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        final Event currentEvent = requestContext.getCurrentEvent();
        LOGGER.debug("Located current event [{}]", currentEvent);

        final Exception error = currentEvent.getAttributes().get("error", Exception.class);
        LOGGER.debug("Located error attribute [{}] with message [{}] from the current event", error.getClass(), error.getMessage());

        final String event = handle(error, requestContext);
        LOGGER.debug("Final event id resolved from the error is [{}]", event);

        return new EventFactorySupport().event(this, event);
    }
}
