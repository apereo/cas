package org.apereo.cas.web.flow.authentication;

import module java.base;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.configuration.model.core.web.MessageBundleProperties;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link CasWebflowExceptionCatalog}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public interface CasWebflowExceptionCatalog {
    /**
     * Unknown event id, error, principal or action.
     */
    String UNKNOWN = "UNKNOWN";

    /**
     * Bean name of the exception catalog.
     */
    String BEAN_NAME = "casWebflowExceptionCatalog";

    Logger LOGGER = LoggerFactory.getLogger(CasWebflowExceptionCatalog.class);

    /**
     * Register exception.
     *
     * @param throwable the throwable
     */
    void registerException(Class<? extends Throwable> throwable);

    /**
     * Register exception.
     *
     * @param throwable the throwable
     */
    void registerExceptions(Collection<Class<? extends Throwable>> throwable);

    /**
     * Gets registered exceptions.
     *
     * @return the registered exceptions
     */
    Set<Class<? extends Throwable>> getRegisteredExceptions();

    /**
     * Translate exception.
     *
     * @param requestContext the request context
     * @param exception      the exception
     * @return the string
     */
    default String translateException(final RequestContext requestContext, final Exception exception) {
        if (exception instanceof final AuthenticationException authenticationException) {
            val values = authenticationException.getHandlerErrors().values().stream().map(Throwable::getClass).toList();
            val handlerErrorName = getRegisteredExceptions()
                .stream()
                .filter(values::contains)
                .map(Class::getSimpleName)
                .findFirst()
                .orElseGet(() -> {
                    LOGGER.debug("Unable to translate handler errors of the authentication exception [{}]. Returning [{}]", exception, UNKNOWN);
                    return UNKNOWN;
                });
            val messageCode = MessageBundleProperties.DEFAULT_BUNDLE_PREFIX_AUTHN_FAILURE + handlerErrorName;
            WebUtils.addErrorMessageToContext(requestContext, messageCode,
                StringUtils.EMPTY, authenticationException.getArgs().toArray());
            return handlerErrorName;
        }

        if (exception instanceof final AbstractTicketException e) {
            val match = getRegisteredExceptions().stream()
                .filter(ex -> ex.isInstance(e)).map(Class::getSimpleName)
                .findFirst();
            WebUtils.addErrorMessageToContext(requestContext, e.getCode(),
                StringUtils.EMPTY, e.getArgs().toArray());
            return match.orElse(CasWebflowExceptionCatalog.UNKNOWN);
        }
        return UNKNOWN;
    }
}
