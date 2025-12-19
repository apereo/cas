package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.authentication.RootCasException;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception that is thrown when an Unauthorized Service attempts to use CAS.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Getter
@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "Unauthorized Service Usage")
public class UnauthorizedServiceException extends RootCasException {

    /**
     * Error code that indicates the service is unauthorized for use.
     **/
    private static final String CODE_UNAUTHORIZED_SERVICE = "screen.service.error.message";

    private static final String CODE_EXPIRED_SERVICE = "screen.service.expired.message";

    private static final String CODE_REQUIRED_SERVICE = "screen.service.required.message";

    private static final String CODE_INITIAL_SERVICE = "screen.service.initial.message";

    @Serial
    private static final long serialVersionUID = 3905807495715960369L;

    protected UnauthorizedServiceException(final String message) {
        this(null, message);
    }

    protected UnauthorizedServiceException(final String code, final String message) {
        super(code, message);
    }

    protected UnauthorizedServiceException(final Throwable cause, final String code, final String message) {
        super(code, message, cause);
    }

    protected UnauthorizedServiceException(final String message, final Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Thrown when service access is rejected.
     *
     * @param message the message
     * @return the runtime exception
     */
    public static RuntimeException denied(final String message) {
        return new UnauthorizedServiceException(CODE_UNAUTHORIZED_SERVICE, message);
    }

    /**
     * Thrown when service in the registry has expired.
     *
     * @param message the message
     * @return the runtime exception
     */
    public static RuntimeException expired(final String message) {
        return new UnauthorizedServiceException(CODE_EXPIRED_SERVICE, message);
    }

    /**
     * Wrap runtime exception.
     *
     * @param ex the ex
     * @return the runtime exception
     */
    public static RuntimeException wrap(final Throwable ex) {
        return new UnauthorizedServiceException(ex, CODE_UNAUTHORIZED_SERVICE, ex.getMessage());
    }

    /**
     * Required exception.
     *
     * @return the runtime exception
     */
    public static RuntimeException required() {
        return new UnauthorizedServiceException(CODE_REQUIRED_SERVICE, CODE_REQUIRED_SERVICE);
    }

    /**
     * Requested service required on initial access.
     *
     * @return the runtime exception
     */
    public static RuntimeException requested() {
        return new UnauthorizedServiceException(CODE_INITIAL_SERVICE, CODE_INITIAL_SERVICE);

    }
}
