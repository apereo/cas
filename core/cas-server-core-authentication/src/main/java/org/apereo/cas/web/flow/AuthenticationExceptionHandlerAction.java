package org.apereo.cas.web.flow;


import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.authentication.adaptive.UnauthorizedAuthenticationException;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.exceptions.InvalidLoginLocationException;
import org.apereo.cas.authentication.exceptions.InvalidLoginTimeException;
import org.apereo.cas.services.UnauthorizedServiceForPrincipalException;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.ticket.UnsatisfiedAuthenticationPolicyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

    private static final String UNKNOWN = "UNKNOWN";

    private static final String DEFAULT_MESSAGE_BUNDLE_PREFIX = "authenticationFailure.";

    /**
     * Default list of errors this class knows how to handle.
     */
    private static final Set<Class<? extends Exception>> DEFAULT_ERROR_LIST = new LinkedHashSet<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationExceptionHandlerAction.class);

    /*
     * Order is important here; We want the account policy exceptions to be handled
     * first before moving onto more generic errors. In the event that multiple handlers
     * are defined, where one failed due to account policy restriction and one fails
     * due to a bad password, we want the error associated with the account policy
     * to be processed first, rather than presenting a more generic error associated
     */
    static {
        DEFAULT_ERROR_LIST.add(javax.security.auth.login.AccountLockedException.class);
        DEFAULT_ERROR_LIST.add(javax.security.auth.login.CredentialExpiredException.class);
        DEFAULT_ERROR_LIST.add(AccountDisabledException.class);
        DEFAULT_ERROR_LIST.add(InvalidLoginLocationException.class);
        DEFAULT_ERROR_LIST.add(AccountPasswordMustChangeException.class);
        DEFAULT_ERROR_LIST.add(InvalidLoginTimeException.class);

        DEFAULT_ERROR_LIST.add(javax.security.auth.login.AccountNotFoundException.class);
        DEFAULT_ERROR_LIST.add(javax.security.auth.login.FailedLoginException.class);
        DEFAULT_ERROR_LIST.add(UnauthorizedServiceForPrincipalException.class);
        DEFAULT_ERROR_LIST.add(PrincipalException.class);
        DEFAULT_ERROR_LIST.add(UnsatisfiedAuthenticationPolicyException.class);
        DEFAULT_ERROR_LIST.add(UnauthorizedAuthenticationException.class);
    }

    /**
     * Ordered list of error classes that this class knows how to handle.
     */
    private Set<Class<? extends Exception>> errors = DEFAULT_ERROR_LIST;

    /**
     * String appended to exception class name to create a message bundle key for that particular error.
     */
    private String messageBundlePrefix = DEFAULT_MESSAGE_BUNDLE_PREFIX;

    /**
     * Gets predefined handled exceptions.
     * with latter handlers.
     *
     * @return the predefined handled exceptions
     */
    public static Set<Class<? extends Exception>> getPredefinedHandledExceptions() {
        return DEFAULT_ERROR_LIST;
    }

    /**
     * Sets the list of custom exceptions that this class knows how to handle.
     * <p>This implementation adds the provided list of exceptions to the default list
     * or just returns if the provided list is empty.
     * <p>This implementation relies on Spring's property source configurer, SpEL, and conversion service
     * infrastructure facilities to convert and inject the collection from cas properties.
     * <p>This method is thread-safe. It should only be called by the Spring container during
     * application context bootstrap
     * or unit tests.
     *
     * @param errors List of errors in order of descending precedence.
     */
    public void setErrors(final List<Class<? extends Exception>> errors) {
        /*
            The specifics of the default empty value: this results in the list with one null element.
            So just get rid of null and have an empty list as a result.
         */
        final List<Class<? extends Exception>> nonNullErrors = errors.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (nonNullErrors.isEmpty()) {
            // Nothing custom provided, so just leave the default list of exceptions alone.
            return;
        }
        /*
            Add the custom exceptions to the tail end of the default list of exceptions.
            Need to do this copy as we have the errors field pointing to DEFAULT_ERROR_LIST statically,
            so not to mutate it.
         */
        this.errors = new LinkedHashSet<>(this.errors);
        this.errors.addAll(nonNullErrors);
    }

    public Set<Class<? extends Exception>> getErrors() {
        return Collections.unmodifiableSet(this.errors);
    }

    /**
     * Package-private helper method to aid in testing.
     *
     * @return true if any custom errors have been added, false otherwise.
     */
    public final boolean containsCustomErrors() {
        return DEFAULT_ERROR_LIST.size() != this.errors.size()
                && this.errors.containsAll(DEFAULT_ERROR_LIST);
    }

    /**
     * Sets the message bundle prefix appended to exception class names to create a message bundle key for that
     * particular error.
     *
     * @param prefix Prefix appended to exception names.
     */
    public void setMessageBundlePrefix(final String prefix) {
        this.messageBundlePrefix = prefix;
    }

    /**
     * Maps an authentication exception onto a state name. Also sets an ERROR severity message in the message context.
     *
     * @param e              Authentication error to handle.
     * @param messageContext the spring message context
     * @return Name of next flow state to transition to or {@value #UNKNOWN}
     */
    public String handle(final Exception e, final MessageContext messageContext) {
        if (e instanceof AuthenticationException) {
            return handleAuthenticationException((AuthenticationException) e, messageContext);
        }

        if (e instanceof AbstractTicketException) {
            return handleAbstractTicketException((AbstractTicketException) e, messageContext);
        }

        // we don't recognize this exception
        LOGGER.trace("Unable to translate errors of the authentication exception [{}]"
                + "Returning [{}]", e, UNKNOWN);
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
     * @param messageContext the spring message context
     * @return Name of next flow state to transition to or {@value #UNKNOWN}
     */
    protected String handleAuthenticationException(final AuthenticationException e,
                                                   final MessageContext messageContext) {
        // find the first error in the error list that matches the handlerErrors
        final String handlerErrorName = this.errors.stream().filter(e.getHandlerErrors().values()::contains)
                .map(Class::getSimpleName).findFirst().orElseGet(() -> {
                    LOGGER.error("Unable to translate handler errors of the authentication exception [{}]"
                            + "Returning [{}]", e, UNKNOWN);
                    return UNKNOWN;
                });

        // output message and return handlerErrorName
        final String messageCode = this.messageBundlePrefix + handlerErrorName;
        messageContext.addMessage(new MessageBuilder().error().code(messageCode).build());
        return handlerErrorName;
    }

    /**
     * Maps an {@link AbstractTicketException} onto a state name equal to the simple class name of the exception with
     * highest precedence. Also sets an ERROR severity message in the message context with the error code found in
     * {@link AbstractTicketException#getCode()}. If no match is found,
     * {@value AuthenticationExceptionHandlerAction#UNKNOWN} is returned.
     *
     * @param e              Ticket exception to handle.
     * @param messageContext the spring message context
     * @return Name of next flow state to transition to or {@value AuthenticationExceptionHandlerAction#UNKNOWN}
     */
    protected String handleAbstractTicketException(final AbstractTicketException e, final MessageContext messageContext) {
        // find the first error in the error list that matches the AbstractTicketException
        final Optional<String> match = this.errors.stream()
                .filter(c -> c.isInstance(e)).map(Class::getSimpleName)
                .findFirst();

        match.ifPresent(s -> messageContext.addMessage(new MessageBuilder().error().code(e.getCode()).build()));
        return match.orElse(UNKNOWN);
    }


    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        final String event = handle(requestContext.getAttributes().get("error", Exception.class),
                requestContext.getMessageContext());
        return new EventFactorySupport().event(this, event);
    }
}
