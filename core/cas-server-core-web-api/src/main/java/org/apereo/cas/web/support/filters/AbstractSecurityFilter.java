package org.apereo.cas.web.support.filters;

import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.util.LoggingUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
        throwException(exception, null);
    }

    protected static void throwException(final Throwable exception, final HttpServletResponse servletResponse) {
        LoggingUtils.error(LOGGER, exception);
        if (exception instanceof UnauthorizedServiceException) {
            servletResponse.setStatus(HttpStatus.FORBIDDEN.value());
        }
        throw exception instanceof final RuntimeException re ? re : new RuntimeException(exception);
    }

}
