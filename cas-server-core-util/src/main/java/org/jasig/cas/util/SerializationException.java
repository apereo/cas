package org.jasig.cas.util;

/**
 * This is for {@link SerializationUtils}.
 *
 * @author Timur Duehr timur.duehr@nccgroup.trust
 * @since 4.3
 */
public class SerializationException extends RuntimeException {
    /**
     * Instantiates a new Deserialization exception.
     *
     * @param msg the message
     */
    public SerializationException(final String msg) {
        super(msg);
    }

    /**
     * Instantiates a new Deserialization exception.
     *
     * @param cause the cause
     */
    public SerializationException(final Throwable cause) {
        super(cause);
    }

    /**
     * Instantiates a new Deserialization exception.
     *
     * @param msg the message
     * @param cause the cause
     */
    public SerializationException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
