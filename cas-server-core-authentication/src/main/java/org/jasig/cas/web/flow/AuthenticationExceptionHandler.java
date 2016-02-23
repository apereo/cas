package org.jasig.cas.web.flow;

import org.jasig.cas.authentication.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.stereotype.Component;

import com.wavity.broker.api.provider.BrokerProvider;
import com.wavity.broker.util.EventAttribute;
import com.wavity.broker.util.EventType;
import com.wavity.broker.util.TopicType;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

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
        DEFAULT_ERROR_LIST.add(javax.security.auth.login.AccountLockedException.class);
        DEFAULT_ERROR_LIST.add(javax.security.auth.login.FailedLoginException.class);
        DEFAULT_ERROR_LIST.add(javax.security.auth.login.CredentialExpiredException.class);
        DEFAULT_ERROR_LIST.add(javax.security.auth.login.AccountNotFoundException.class);
        DEFAULT_ERROR_LIST.add(org.jasig.cas.authentication.AccountDisabledException.class);
        DEFAULT_ERROR_LIST.add(org.jasig.cas.authentication.InvalidLoginLocationException.class);
        DEFAULT_ERROR_LIST.add(org.jasig.cas.authentication.AccountPasswordMustChangeException.class);
        DEFAULT_ERROR_LIST.add(org.jasig.cas.authentication.InvalidLoginTimeException.class);
    }

    /** Ordered list of error classes that this class knows how to handle. */
    @NotNull
    private List<Class<? extends Exception>> errors = DEFAULT_ERROR_LIST;

    /** String appended to exception class name to create a message bundle key for that particular error. */
    private String messageBundlePrefix = DEFAULT_MESSAGE_BUNDLE_PREFIX;

    /**
     * Sets the list of errors that this class knows how to handle.
     *
     * @param errors List of errors in order of descending precedence.
     */
    public void setErrors(final List<Class<? extends Exception>> errors) {
        this.errors = errors;
    }

    public final List<Class<? extends Exception>> getErrors() {
        return Collections.unmodifiableList(this.errors);
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
     * Maps an authentication exception onto a state name equal to the simple class name of the.
     *
     * @param e Authentication error to handle.
     * @param messageContext the spring message context
     * @return Name of next flow state to transition to or {@value #UNKNOWN}
     * {@link org.jasig.cas.authentication.AuthenticationException#getHandlerErrors()} with highest precedence.
     * Also sets an ERROR severity message in the message context of the form
     * {@code [messageBundlePrefix][exceptionClassSimpleName]} for each handler error that is
     * configured. If not match is found, {@value #UNKNOWN} is returned.
     */
    public String handle(final AuthenticationException e, final MessageContext messageContext) {
        if (e != null) {
            final MessageBuilder builder = new MessageBuilder();
            for (final Class<? extends Exception> kind : this.errors) {
                for (final Class<? extends Exception> handlerError : e.getHandlerErrors().values()) {
                    if (handlerError != null && handlerError.equals(kind)) {
                        final String handlerErrorName = handlerError.getSimpleName();
                        final String messageCode = this.messageBundlePrefix + handlerErrorName;
                        messageContext.addMessage(builder.error().code(messageCode).build());
                        produceBrokerMessage(handlerErrorName, messageCode);
                        return handlerErrorName;
                    }
                }

            }
        }
        final String messageCode = this.messageBundlePrefix + UNKNOWN;
        logger.trace("Unable to translate handler errors of the authentication exception {}. Returning {} by default...", e, messageCode);
        produceBrokerMessage(messageCode);
        messageContext.addMessage(new MessageBuilder().error().code(messageCode).build());
        return UNKNOWN;
    }
    
    /**
     * Produces broker message when a handler error occurred
     * 
     * @param handlerErrorName the string of the handler error name
     * @param messageCode the string of the message code
     */
    private final void produceBrokerMessage(final String handlerErrorName, final String messageCode) {
    	if (("".equals(handlerErrorName) || handlerErrorName == null) || ("".equals(messageCode) || messageCode == null)) {
    		logger.error("*** handlerErrorName and messageCode required to publish a message ***");
    		return;
    	}
    	publishMessage(String.format("handler error: %s, message code: %s", handlerErrorName, messageCode));
    }
    
    /**
     * Produces broker message when a unknown error happened
     * 
     * @param messageCode the string of message code
     */
    private final void produceBrokerMessage(final String messageCode) {
    	if ("".equals(messageCode) || messageCode == null) {
    		logger.error("*** message code is required to publish a message ***");
    		return;
    	}
    	publishMessage(messageCode);
    }
    
    /**
     * Publishes a message to KAFKA
     * 
     * @param message the string of message
     */
    private final void publishMessage(final String message) {
    	if ("".equals(message) || message == null) {
    		logger.error("*** the message is required to pubish an error message ***");
    		return;
    	}
    	try {
    		final BrokerProvider brokerProvider = BrokerProvider.getInstance();
			final EnumMap<EventAttribute, Object> attr = new EnumMap<EventAttribute, Object>(EventAttribute.class);
			attr.put(EventAttribute.MESSAGE, String.format("Credential: %s, message: %s", AuthUtils.getCredential(), message));
			attr.put(EventAttribute.TIMESTAMP, Long.toString(Calendar.getInstance().getTimeInMillis()));
			attr.put(EventAttribute.IS_NOTIFY_TARGET, true);
			attr.put(EventAttribute.ACTOR_ID, AuthUtils.getTenantId());
			attr.put(EventAttribute.EVENT_RESULT, "success");
			attr.put(EventAttribute.EC_ID, "Test EC ID");
			brokerProvider.publish(TopicType.ADMIN, EventType.EVENT_TYPE_SSO_AUTHENTICATION, attr);
		} catch (final Exception e) {
			logger.error("broker failed to publish event", e);
		}
    }
}
