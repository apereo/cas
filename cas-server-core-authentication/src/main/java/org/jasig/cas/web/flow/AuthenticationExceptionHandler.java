package org.jasig.cas.web.flow;

import org.jasig.cas.authentication.AccountDisabledException;
import org.jasig.cas.authentication.AccountPasswordMustChangeException;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.InvalidLoginLocationException;
import org.jasig.cas.authentication.InvalidLoginTimeException;
import org.jasig.cas.services.UnauthorizedServiceForPrincipalException;
import org.jasig.cas.ticket.AbstractTicketException;
import org.jasig.cas.ticket.UnsatisfiedAuthenticationPolicyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.stereotype.Component;

import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.stream.Collectors.toList;


/**
 * Performs two important error handling functions on an {@link AuthenticationException} raised from the authentication
 * layer:
 *
 * <ol>
 *     <li>Maps handler errors onto message bundle strings for display to user.</li>
 *     <li>Determines the next webflow state by comparing handler errors against {@link #errors}
 *     in list order. The first entry that matches determines the outcome state, which
 *     is the simple class name of the exception.</li>
 * </ol>
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Component("authenticationExceptionHandler")
public class AuthenticationExceptionHandler {

    /** State name when no matching exception is found. */
    private static final String UNKNOWN = "UNKNOWN";

    /** Default message bundle prefix. */
    private static final String DEFAULT_MESSAGE_BUNDLE_PREFIX = "authenticationFailure.";

    /** Default list of errors this class knows how to handle. */
    private static final List<Class<? extends Exception>> DEFAULT_ERROR_LIST =
            new ArrayList<>();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    static {
        DEFAULT_ERROR_LIST.add(AccountLockedException.class);
        DEFAULT_ERROR_LIST.add(FailedLoginException.class);
        DEFAULT_ERROR_LIST.add(CredentialExpiredException.class);
        DEFAULT_ERROR_LIST.add(AccountNotFoundException.class);
        DEFAULT_ERROR_LIST.add(AccountDisabledException.class);
        DEFAULT_ERROR_LIST.add(InvalidLoginLocationException.class);
        DEFAULT_ERROR_LIST.add(AccountPasswordMustChangeException.class);
        DEFAULT_ERROR_LIST.add(InvalidLoginTimeException.class);
        DEFAULT_ERROR_LIST.add(UnauthorizedServiceForPrincipalException.class);
        DEFAULT_ERROR_LIST.add(UnsatisfiedAuthenticationPolicyException.class);
    }

    /** Ordered list of error classes that this class knows how to handle. */
    @NotNull
    private List<Class<? extends Exception>> errors = DEFAULT_ERROR_LIST;

    /** String appended to exception class name to create a message bundle key for that particular error. */
    private String messageBundlePrefix = DEFAULT_MESSAGE_BUNDLE_PREFIX;

    /**
     * Sets the list of custom exceptions that this class knows how to handle.
     *
     * This implementation adds the provided list of exceptions to the default list
     * or just returns if the provided list is empty.
     *
     * This implementation relies on Spring's property source configurer, SpEL, and conversion service
     * infrastructure facilities to convert and inject the collection from cas.properties.
     *
     * This method is thread-safe. It should only be called by the Spring container during application context bootstrap
     * or unit tests.
     *
     * @param errors List of errors in order of descending precedence.
     */
    @Value("#{'${cas.custom.authentication.exceptions:}'.split(',')}")
    public void setErrors(final List<Class<? extends Exception>> errors) {
        //The specifics of the default empty value: this results in the list with one null element. So just get rid of null and have
        //an empty list as a result.
        final List<Class<? extends Exception>> nonNullErrors = errors.stream().filter(Objects::nonNull).collect(toList());
        if(nonNullErrors.size() == 0) {
            //Nothing custom provided, so just leave the default list of exceptions alone.
            return;
        }
        //Add the custom exceptions to the tail end of the default list of exceptions.
        //Need to do this copy as we have the errors field pointing to DEFAULT_ERROR_LIST statically, so not to mutate it.
        this.errors = new ArrayList<>(this.errors);
        this.errors.addAll(nonNullErrors);
    }

    public final List<Class<? extends Exception>> getErrors() {
        return Collections.unmodifiableList(this.errors);
    }

    /**
     * Package-private helper method to aid in testing.
     *
     * @return true if any custom errors have been added, false otherwise.
     */
    final boolean containsCustomErrors() {
        return !(DEFAULT_ERROR_LIST.size() == this.errors.size()) && (this.errors.containsAll(DEFAULT_ERROR_LIST));
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
     * @param e Authentication error to handle.
     * @param messageContext the spring message context
     * @return Name of next flow state to transition to or {@value #UNKNOWN}
     */
    public String handle(final Exception e, final MessageContext messageContext) {
        if (e instanceof AuthenticationException) {
            return handleAuthenticationException((AuthenticationException) e, messageContext);
        } else if (e instanceof AbstractTicketException) {
            return handleAbstractTicketException((AbstractTicketException) e, messageContext);
        }

        // we don't recognize this exception
        logger.trace("Unable to translate handler errors of the authentication exception {}. "
                + "Returning {} by default...", e, UNKNOWN);
        final String messageCode = this.messageBundlePrefix + UNKNOWN;
        messageContext.addMessage(new MessageBuilder().error().code(messageCode).build());
        return UNKNOWN;
    }

    /**
     * Maps an authentication exception onto a state name equal to the simple class name of the {@link
     * AuthenticationException#getHandlerErrors()} with highest precedence. Also sets an ERROR severity message in the
     * message context of the form {@code [messageBundlePrefix][exceptionClassSimpleName]} for for the first handler
     * error that is configured. If no match is found, {@value #UNKNOWN} is returned.
     *
     * @param e              Authentication error to handle.
     * @param messageContext the spring message context
     * @return Name of next flow state to transition to or {@value #UNKNOWN}
     */
    protected String handleAuthenticationException(final AuthenticationException e, final MessageContext messageContext) {
        // find the first error in the error list that matches the handlerErrors
        final String handlerErrorName = this.errors.stream().filter(e.getHandlerErrors().values()::contains)
                .map(Class::getSimpleName).findFirst().orElseGet(() -> {
                    logger.error("Unable to translate handler errors of the authentication exception {}. "
                            + "Returning {} by default...", e, UNKNOWN);
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
     * {@link AbstractTicketException#getCode()}. If no match is found, {@value #UNKNOWN} is returned.
     *
     * @param e              Ticket exception to handle.
     * @param messageContext the spring message context
     * @return Name of next flow state to transition to or {@value #UNKNOWN}
     */
    protected String handleAbstractTicketException(final AbstractTicketException e, final MessageContext messageContext) {
        // find the first error in the error list that matches the AbstractTicketException
        final Optional<String> match = this.errors.stream().filter((c) -> c.isInstance(e)).map(Class::getSimpleName)
                .findFirst();

        // for AbstractTicketExceptions we only output messages for errors in the errors list
        if (match.isPresent()) {
            // use the RootCasException.getCode() for the message code
            messageContext.addMessage(new MessageBuilder().error().code(((AbstractTicketException) e).getCode())
                    .build());
        }

        // return the matched simple class name
        return match.orElse(UNKNOWN);
    }
}
