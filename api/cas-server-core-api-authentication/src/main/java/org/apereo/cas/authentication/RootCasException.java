package org.apereo.cas.authentication;


import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * Generic CAS exception that sits at the top of the exception hierarchy. Provides
 * unified logic around retrieval and configuration of exception codes that may be
 * mapped inside an external resource bundle for internationalization of error messages.
 *
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@Slf4j
@ToString
@AllArgsConstructor
public abstract class RootCasException extends RuntimeException {

    private static final long serialVersionUID = -2384466176716541689L;

    /**
     * The code description of the exception.
     */
    private final String code;

    /**
     * Constructs a new exception with the code identifying the exception
     * and the error message.
     *
     * @param code the code to describe what type of exception this is.
     * @param msg  The error message associated with this exception for additional logging purposes.
     */
    public RootCasException(final String code, final String msg) {
        super(msg);
        this.code = code;
    }

    /**
     * Constructs a new exception with the code identifying the exception
     * and the original throwable.
     *
     * @param code      the code to describe what type of exception this is.
     * @param throwable the original exception we are chaining.
     */
    public RootCasException(final String code, final Throwable throwable) {
        super(throwable);
        this.code = code;
    }

    /**
     * @return Returns the code. If there is a chained exception it recursively
     * calls {@code getCode()} on the cause of the chained exception rather than the returning
     * the code itself.
     */
    public String getCode() {
        final Throwable cause = this.getCause();
        if (cause instanceof RootCasException) {
            return ((RootCasException) cause).getCode();
        }
        return this.code;
    }
}
