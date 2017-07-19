package org.apereo.cas.authentication.adaptive;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.HandlerResult;

import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link UnauthorizedAuthenticationException}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class UnauthorizedAuthenticationException extends AuthenticationException {

    private static final long serialVersionUID = 4386330975702952112L;

    public UnauthorizedAuthenticationException(final String message, final Map<String, Class<? extends Throwable>> handlerErrors) {
        super(message, handlerErrors, new HashMap<>(0));
    }
    
    public UnauthorizedAuthenticationException(final Map<String, Class<? extends Throwable>> handlerErrors) {
        super(handlerErrors);
    }

    public UnauthorizedAuthenticationException(final Map<String, Class<? extends Throwable>> handlerErrors, final Map<String, HandlerResult> handlerSuccesses) {
        super(handlerErrors, handlerSuccesses);
    }

    public UnauthorizedAuthenticationException(final String message, 
                                               final Map<String, Class<? extends Throwable>> handlerErrors,
                                               final Map<String, HandlerResult> handlerSuccesses) {
        super(message, handlerErrors, handlerSuccesses);
    }
}
