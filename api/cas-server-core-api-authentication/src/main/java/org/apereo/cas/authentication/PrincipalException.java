package org.apereo.cas.authentication;

import java.util.Map;

/**
 * Describes a principal resolution error, which is a subcategory of authentication error.
 * Principal resolution necessarily happens after successful authentication for a given credential.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class PrincipalException extends AuthenticationException {

    /** Serialization metadata. */
    private static final long serialVersionUID = -6590363469748313596L;

    /**
     * Creates a new instance.
     * @param message Error message.
     * @param handlerErrors Map of handler names to errors.
     * @param handlerSuccesses Map of handler names to authentication successes.
     */
    public PrincipalException(
            final String message,
            final Map<String, Class<? extends Throwable>> handlerErrors,
            final Map<String, HandlerResult> handlerSuccesses) {
        super(message, handlerErrors, handlerSuccesses);
    }
}
