package org.apereo.cas.web.flow.authentication;

import org.apereo.cas.configuration.model.core.web.MessageBundleProperties;
import org.apereo.cas.web.support.WebUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
        LOGGER.trace("Unable to translate errors of the authentication exception [{}]. Returning [{}]",
            exception, CasWebflowExceptionCatalog.UNKNOWN);
        addErrorMessageToContext(exception, requestContext);
        return EVENT_FACTORY.event(this, CasWebflowExceptionCatalog.UNKNOWN);
    }

    @Override
    public boolean supports(final Exception exception, final RequestContext requestContext) {
        return exception != null;
    }

    protected void addErrorMessageToContext(final Exception exception, final RequestContext requestContext) {
        val messageCode = MessageBundleProperties.DEFAULT_BUNDLE_PREFIX_AUTHN_FAILURE + CasWebflowExceptionCatalog.UNKNOWN;
        WebUtils.addErrorMessageToContext(requestContext, messageCode);
    }
}
