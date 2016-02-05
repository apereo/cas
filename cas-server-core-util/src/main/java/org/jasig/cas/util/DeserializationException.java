package org.jasig.cas.util;

/**
 * This is for {@link SerializationUtils}.
 *
 * @author Timur Duehr timur.duehr@nccgroup.trust
 * @since 4.3
 */
public class DeserializationException extends RuntimeException {
    /**
     * Instantiates a new Deserialization exception.
     *
     * @param e the cause
     */
    public DeserializationException(final Throwable e) {
        super(e);
    }

    /**
     * Instantiates a new Deserialization exception.
     *
     * @param msg the message
     */
    public DeserializationException(final String msg) {
        super(msg);
    }

    /**
     * Instantiates a new Deserialization exception.
     *
     * @param msg the message
     * @param cause the cause
     */
    public DeserializationException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
