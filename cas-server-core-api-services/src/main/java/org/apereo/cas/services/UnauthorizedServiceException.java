package org.apereo.cas.services;

/**
 * Exception that is thrown when an Unauthorized Service attempts to use CAS.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class UnauthorizedServiceException extends RuntimeException {

    /** Error code that indicates the service is unauthorized for use. **/
    public static final String CODE_UNAUTHZ_SERVICE = "screen.service.error.message";
    
    /** Exception object that indicates the service manager is empty with no service definitions. **/
    public static final String CODE_EMPTY_SVC_MGMR = "screen.service.empty.error.message";

    /** The Unique ID for serialization. */
    private static final long serialVersionUID = 3905807495715960369L;

    private final String code;
    
    /**
     * Construct the exception object with the associated error code.
     * @param message the error message
     */
    public UnauthorizedServiceException(final String message) {
        this(null, message);
    }
    
    /**
     * Constructs an UnauthorizedServiceException with a custom message and the
     * root cause of this exception.
     *
     * @param message an explanatory message. Maybe null or blank.
     * @param code the error code mapped to the messaged bundle.
     */
    public UnauthorizedServiceException(final String code, final String message) {
        super(message);
        this.code = code;
    }
    /**
     * Constructs an UnauthorizedServiceException with a custom message and the
     * root cause of this exception.
     *
     * @param message an explanatory message.
     * @param cause the root cause of the exception.
     */
    public UnauthorizedServiceException(final String message, final Throwable cause) {
        super(message, cause);
        this.code = null;
    }
    
    /**
     * The error code associated with this exception.
     * @return the error code.
     */
    public String getCode() {
        return this.code;
    }
}
