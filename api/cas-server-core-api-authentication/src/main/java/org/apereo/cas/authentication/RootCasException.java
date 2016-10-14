package org.apereo.cas.authentication;


/**
 * Generic CAS exception that sits at the top of the exception hierarchy. Provides
 * unified logic around retrieval and configuration of exception codes that may be
 * mapped inside an external resource bundle for internationalization of error messages.
 *
 * @author Misagh Moayyed
 * @since 4.0.0
 */
public abstract class RootCasException extends RuntimeException {

    private static final long serialVersionUID = -2384466176716541689L;

    /**
     * The code description of the exception.
     */
    private final String code;

    /**
     * Constructor that takes a {@code code} description of the error along with the exception
     * {@code msg} generally for logging purposes. These codes normally have a corresponding
     * entries in the messages file for the internationalization of error messages.
     *
     * @param code the code to describe what type of exception this is.
     */
    public RootCasException(final String code) {
        this.code = code;
    }

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

    @Override
    public String toString() {
        return this.getCode();
    }
}
