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

    private static final long serialVersionUID = -6590363469748313596L;

    private static final String CODE = "service.principal.resolution.error";

    /**
     * Creates a new instance.
     *
     * @param message          Error message.
     * @param handlerErrors    Map of handler names to errors.
     * @param handlerSuccesses Map of handler names to authentication successes.
     */
    public PrincipalException(
        final String message,
        final Map<String, Throwable> handlerErrors,
        final Map<String, AuthenticationHandlerExecutionResult> handlerSuccesses) {
        super(message, handlerErrors, handlerSuccesses);
    }

    @Override
    public String getCode() {
        return CODE;
    }
}
