package org.apereo.cas.web.flow.authentication;

import org.apereo.cas.configuration.model.core.web.MessageBundleProperties;
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
    private final CasWebflowExceptionCatalog errors;

    private int order = Integer.MAX_VALUE;

    @Override
    public Event handle(final Exception exception, final RequestContext requestContext) {
        val messageContext = requestContext.getMessageContext();
        LOGGER.trace("Unable to translate errors of the authentication exception [{}]. Returning [{}]",
            exception, CasWebflowExceptionCatalog.UNKNOWN);
        val message = buildErrorMessageResolver(exception, requestContext);
        messageContext.addMessage(message);
        return new EventFactorySupport().event(this, CasWebflowExceptionCatalog.UNKNOWN);
    }

    @Override
    public boolean supports(final Exception exception, final RequestContext requestContext) {
        return exception != null;
    }

    /**
     * Build error message resolver.
     *
     * @param exception      the exception
     * @param requestContext the request context
     * @return the message resolver
     */
    protected MessageResolver buildErrorMessageResolver(final Exception exception, final RequestContext requestContext) {
        val messageCode = MessageBundleProperties.DEFAULT_BUNDLE_PREFIX_AUTHN_FAILURE + CasWebflowExceptionCatalog.UNKNOWN;
        return new MessageBuilder()
            .error()
            .code(messageCode)
            .build();
    }
}
