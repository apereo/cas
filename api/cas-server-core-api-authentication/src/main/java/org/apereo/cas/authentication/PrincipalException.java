package org.apereo.cas.authentication;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Describes a principal resolution error, which is a subcategory of authentication error.
 * Principal resolution necessarily happens after successful authentication for a given credential.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Slf4j
@Getter
public class PrincipalException extends AuthenticationException {

    /** Serialization metadata. */
    private static final long serialVersionUID = -6590363469748313596L;

    private final String code = "service.principal.resolution.error";

    /**
     * Creates a new instance.
     * @param message Error message.
     * @param handlerErrors Map of handler names to errors.
     * @param handlerSuccesses Map of handler names to authentication successes.
     */
    public PrincipalException(
            final String message,
            final Map<String, Throwable> handlerErrors,
            final Map<String, AuthenticationHandlerExecutionResult> handlerSuccesses) {
        super(message, handlerErrors, handlerSuccesses);
    }
}
