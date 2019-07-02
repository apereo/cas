package org.apereo.cas.web.flow.authentication;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageResolver;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Set;

/**
 * This is {@link GenericCasWebflowExceptionHandler}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Getter
@Setter
@Slf4j
@RequiredArgsConstructor
public class GenericCasWebflowExceptionHandler implements CasWebflowExceptionHandler<Exception> {
    private int order = Integer.MAX_VALUE;

    /**
     * Ordered list of error classes that this class knows how to handle.
     */
    private final Set<Class<? extends Throwable>> errors;

    /**
     * String appended to exception class name to create a message bundle key for that particular error.
     */
    private final String messageBundlePrefix;

    @Override
    public Event handle(final Exception exception, final RequestContext requestContext) {
        val messageContext = requestContext.getMessageContext();
        LOGGER.trace("Unable to translate errors of the authentication exception [{}]. Returning [{}]", exception, CasWebflowExceptionHandler.UNKNOWN);
        val message = buildErrorMessageResolver(exception, requestContext);
        messageContext.addMessage(message);
        return new EventFactorySupport().event(this, CasWebflowExceptionHandler.UNKNOWN);
    }

    /**
     * Build error message resolver.
     *
     * @param exception      the exception
     * @param requestContext the request context
     * @return the message resolver
     */
    protected MessageResolver buildErrorMessageResolver(final Exception exception, final RequestContext requestContext) {
        val messageCode = this.messageBundlePrefix + CasWebflowExceptionHandler.UNKNOWN;
        return new MessageBuilder()
            .error()
            .code(messageCode)
            .build();
    }

    @Override
    public boolean supports(final Exception exception, final RequestContext requestContext) {
        return exception != null;
    }
}
