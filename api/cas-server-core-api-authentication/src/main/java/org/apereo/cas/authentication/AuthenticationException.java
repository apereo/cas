package org.apereo.cas.authentication;

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
public class AuthenticationException extends RuntimeException {

    /** Serialization metadata. */
    private static final long serialVersionUID = -6032827784134751797L;

    /** Immutable map of handler names to the errors they raised. */
    private final Map<String, Class<? extends Throwable>> handlerErrors;

    /** Immutable map of handler names to an authentication success metadata instance. */
    private final Map<String, HandlerResult> handlerSuccesses;

    /**
     * Creates a new instance for the case when no handlers were attempted, i.e. no successes or failures.
     *
     * @param msg the msg
     */
    public AuthenticationException(final String msg) {
        this(
            msg,
            new HashMap<>(0),
            new HashMap<>(0));
    }

    /**
     * Instantiates a new Authentication exception.
     */
    public AuthenticationException() {
        this("No supported authentication handlers found for given credentials");
    }

    /**
     * Creates a new instance for the case when no handlers succeeded.
     *
     * @param handlerErrors Map of handler names to errors.
     */
    public AuthenticationException(final Map<String, Class<? extends Throwable>> handlerErrors) {
        this(handlerErrors, new HashMap<>(0));
    }

    /**
     * Creates a new instance for the case when there are both handler successes and failures.
     *
     * @param handlerErrors Map of handler names to errors.
     * @param handlerSuccesses Map of handler names to authentication successes.
     */
    public AuthenticationException(final Map<String, Class<? extends Throwable>> handlerErrors, 
                                   final Map<String, HandlerResult> handlerSuccesses) {
        this(
            String.format("%s errors, %s successes", handlerErrors.size(), handlerSuccesses.size()),
            handlerErrors,
            handlerSuccesses);
    }

    /**
     * Creates a new instance for the case when there are both handler successes and failures and a custom
     * error message is required.
     *
     * @param message the message associated with this error.
     * @param handlerErrors Map of handler names to errors.
     * @param handlerSuccesses Map of handler names to authentication successes.
     */
    public AuthenticationException(
            final String message,
            final Map<String, Class<? extends Throwable>> handlerErrors,
            final Map<String, HandlerResult> handlerSuccesses) {
        super(message);
        this.handlerErrors = new HashMap<>(handlerErrors);
        this.handlerSuccesses = new HashMap<>(handlerSuccesses);
    }

    /**
     * Gets an unmodifiable map of handler names to errors.
     *
     * @return Immutable map of handler names to errors.
     */
    public Map<String, Class<? extends Throwable>> getHandlerErrors() {
        return this.handlerErrors;
    }

    /**
     * Gets an unmodifiable map of handler names to authentication successes.
     *
     * @return Immutable map of handler names to authentication successes.
     */
    public Map<String, HandlerResult> getHandlerSuccesses() {
        return this.handlerSuccesses;
    }
}
