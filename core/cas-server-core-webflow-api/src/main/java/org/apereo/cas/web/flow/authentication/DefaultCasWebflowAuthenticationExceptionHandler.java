package org.apereo.cas.web.flow.authentication;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.services.UnauthorizedServiceForPrincipalException;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link DefaultCasWebflowAuthenticationExceptionHandler}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Getter
@Setter
@Slf4j
@RequiredArgsConstructor
public class DefaultCasWebflowAuthenticationExceptionHandler implements CasWebflowExceptionHandler<AuthenticationException> {
    /**
     * Ordered list of error classes that this class knows how to handle.
     */
    private final CasWebflowExceptionCatalog errors;

    /**
     * String appended to exception class name to create a message bundle key for that particular error.
     */
    private final String messageBundlePrefix;

    private int order = Integer.MAX_VALUE - 1;

    @Override
    public Event handle(final AuthenticationException exception, final RequestContext requestContext) {
        val id = handleAuthenticationException(exception, requestContext);
        return new EventFactorySupport().event(this, id);
    }

    @Override
    public boolean supports(final Exception exception, final RequestContext requestContext) {
        return exception instanceof AuthenticationException;
    }

    /**
     * Maps an authentication exception onto a state name equal to the simple class name of the handler errors.
     * with highest precedence. Also sets an ERROR severity message in the
     * message context of the form {@code [messageBundlePrefix][exceptionClassSimpleName]}
     * for for the first handler
     * error that is configured. If no match is found, {@value #UNKNOWN} is returned.
     *
     * @param exception              Authentication error to handle.
     * @param requestContext the spring context
     * @return Name of next flow state to transition to or {@value #UNKNOWN}
     */
    protected String handleAuthenticationException(final AuthenticationException exception, final RequestContext requestContext) {
        if (exception.getHandlerErrors().containsKey(UnauthorizedServiceForPrincipalException.class.getSimpleName())) {
            val url = WebUtils.getUnauthorizedRedirectUrlFromFlowScope(requestContext);
            if (url != null) {
                LOGGER.warn("Unauthorized service access for principal; CAS will be redirecting to [{}]", url);
                return CasWebflowConstants.STATE_ID_SERVICE_UNAUTHZ_CHECK;
            }
        }
        val values = exception.getHandlerErrors().values().stream().map(Throwable::getClass).toList();
        val handlerErrorName = errors.getRegisteredExceptions()
            .stream()
            .filter(values::contains)
            .map(Class::getSimpleName)
            .findFirst()
            .orElseGet(() -> {
                LOGGER.debug("Unable to translate handler errors of the authentication exception [{}]. Returning [{}]", exception, UNKNOWN);
                return UNKNOWN;
            });

        val messageCode = this.messageBundlePrefix + handlerErrorName;
        WebUtils.addErrorMessageToContext(requestContext, messageCode, StringUtils.EMPTY, exception.getArgs().toArray());
        return handlerErrorName;
    }
}
