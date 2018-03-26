package org.apereo.cas.support.jpa;

/**
 * Exception to wrap checked exceptions in closures used in JPA.
 * @author Timur Duehr
 * @since 5.3.0
 */
public class JpaRuntimeException extends RuntimeException {
    public JpaRuntimeException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public JpaRuntimeException(final String message) {
        super(message);
    }
}
