package org.apereo.cas.support.rest;

/**
 * This is {@link BadRequestException}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class BadRequestException extends IllegalArgumentException {
    private static final long serialVersionUID = 6852720596988243487L;

    /**
     * Ctor.
     *
     * @param msg error message
     */
    public BadRequestException(final String msg) {
        super(msg);
    }
}
