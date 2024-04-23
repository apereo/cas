package org.apereo.cas.web.support.filters;

import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.util.LoggingUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.webflow.conversation.NoSuchConversationException;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link AbstractSecurityFilter}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@Setter
@Getter
public abstract class AbstractSecurityFilter {
    protected static void throwException(final Throwable exception) {
        throwException(exception, null, null);
    }

    protected static void throwException(final Throwable exception, final HttpServletResponse servletResponse,
                                         final HttpServletRequest servletRequest) {
        LoggingUtils.error(LOGGER, exception);
        if (servletResponse != null) {
            if (exception instanceof UnauthorizedServiceException) {
                servletResponse.setStatus(HttpStatus.FORBIDDEN.value());
            }
            val nsce = (RuntimeException) getRootCause(exception, NoSuchConversationException.class);
            if (nsce != null) {
                servletRequest.setAttribute(RequestDispatcher.ERROR_EXCEPTION, nsce);
                throw nsce;
            }
        }
        throw exception instanceof final RuntimeException re ? re : new RuntimeException(exception);
    }

    private static Throwable getRootCause(final Throwable exception, final Class<?> cause) {
        var rootCause = exception.getCause();
        if (rootCause != null) {
            do {
                if (rootCause.getClass().equals(cause)) {
                    return rootCause;
                }
                rootCause = rootCause.getCause();
            } while (rootCause != null);
        }
        return null;
    }
}
