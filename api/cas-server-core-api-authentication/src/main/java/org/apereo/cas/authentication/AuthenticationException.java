package org.apereo.cas.authentication;

import lombok.Getter;

import java.io.Serial;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Authentication raised by {@link AuthenticationManager} to signal authentication failure.
 * Authentication failure typically occurs when one or more {@link AuthenticationHandler} components
 * fail to authenticate credentials. This exception contains information about handler successes
 * and failures that may be used by higher-level components to determine subsequent behavior.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Getter
public class AuthenticationException extends RootCasException {

    private static final String CODE = "INVALID_AUTHN_REQUEST";

    @Serial
    private static final long serialVersionUID = -6032827784134751797L;

    private final Map<String, Throwable> handlerErrors;

    private final Map<String, AuthenticationHandlerExecutionResult> handlerSuccesses;

    public AuthenticationException(final String msg) {
        this(msg, new HashMap<>(), new HashMap<>());
    }

    public AuthenticationException() {
        this("The authentication attempt has failed for given credentials");
    }

    /**
     * Creates a new instance for the case when no handlers succeeded.
     *
     * @param handlerErrors Map of handler names to errors.
     */
    public AuthenticationException(final Map<String, Throwable> handlerErrors) {
        this(handlerErrors, new HashMap<>());
    }

    public AuthenticationException(final Throwable handlerError) {
        this(Collections.singletonMap(handlerError.getClass().getSimpleName(), handlerError));
        initCause(handlerError);
    }

    public AuthenticationException(final Map<String, Throwable> handlerErrors,
                                   final Map<String, AuthenticationHandlerExecutionResult> handlerSuccesses) {
        this(String.format("%s errors, %s successes", handlerErrors.size(), handlerSuccesses.size()), handlerErrors, handlerSuccesses);
    }

    public AuthenticationException(final String message, final Map<String, Throwable> handlerErrors,
                                   final Map<String, AuthenticationHandlerExecutionResult> handlerSuccesses) {
        super(CODE, message);
        this.handlerErrors = new HashMap<>(handlerErrors);
        this.handlerSuccesses = new HashMap<>(handlerSuccesses);
    }
}
