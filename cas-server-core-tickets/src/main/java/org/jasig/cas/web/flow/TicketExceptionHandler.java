package org.jasig.cas.web.flow;

import com.google.common.collect.ImmutableList;
import org.jasig.cas.ticket.AbstractTicketException;
import org.jasig.cas.ticket.UnsatisfiedAuthenticationPolicyException;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Performs two important error handling functions on an {@link AbstractTicketException} raised from the login webflow
 * layer:
 *
 * <ol>
 *     <li>Maps ticket errors onto message bundle strings for display to user for configured errors only.</li>
 *     <li>Determines the next webflow state by comparing ticket errors against {@link #errors} in list order. The first
 *     entry that matches determines the outcome state, which is the simple class name of the exception.</li>
 * </ol>
 *
 * @author Daniel Frett
 * @since 4.3.0
 */
@Component("ticketExceptionHandler")
public class TicketExceptionHandler {
    /** State name when no matching exception is found. */
    private static final String UNKNOWN = "UNKNOWN";

    /** Default message bundle prefix. */
    private static final String DEFAULT_MESSAGE_BUNDLE_PREFIX = "ticketFailure.";

    /** Default list of errors this class knows how to handle. */
    private static final List<Class<? extends AbstractTicketException>> DEFAULT_ERROR_LIST = ImmutableList.of(
            UnsatisfiedAuthenticationPolicyException.class
    );

    /** Ordered list of error classes that this class knows how to handle. */
    @NotNull
    private List<Class<? extends AbstractTicketException>> errors = DEFAULT_ERROR_LIST;

    /** String appended to exception class name to create a message bundle key for that particular error. */
    private String messageBundlePrefix = DEFAULT_MESSAGE_BUNDLE_PREFIX;

    /**
     * Sets the list of errors that this class knows how to handle.
     *
     * @param errors List of errors in order of descending precedence.
     */
    public void setErrors(final List<Class<? extends AbstractTicketException>> errors) {
        this.errors = errors != null ? ImmutableList.copyOf(errors) : ImmutableList.of();
    }

    public final List<Class<? extends AbstractTicketException>> getErrors() {
        return ImmutableList.copyOf(this.errors);
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
     * Maps an {@link AbstractTicketException} onto a state name equal to the simple class name of the exception with
     * highest precedence. Also sets an ERROR severity message in the message context of the form {@code
     * [messageBundlePrefix][exceptionClassSimpleName]} for each handler error that is configured. If no match is found,
     * {@value #UNKNOWN} is returned.
     *
     * @param e              Authentication error to handle.
     * @param messageContext the spring message context
     * @return Name of next flow state to transition to or {@value #UNKNOWN}
     */
    public String handle(final AbstractTicketException e, final MessageContext messageContext) {
        // find the first error class that e extends
        final Class<? extends AbstractTicketException> error = this.errors.stream().filter((c) -> c.isInstance(e))
                .findFirst().orElse(null);

        // only output message if we found an error
        if (error != null) {
            final String ticketErrorName = error.getSimpleName();
            final String messageCode = this.messageBundlePrefix + ticketErrorName;
            messageContext.addMessage(new MessageBuilder().error().code(messageCode).build());
            return ticketErrorName;
        }

        // default to UNKNOWN state without any message
        return UNKNOWN;
    }
}
