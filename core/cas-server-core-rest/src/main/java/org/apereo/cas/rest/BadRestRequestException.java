package org.apereo.cas.rest;


/**
 * This is {@link BadRestRequestException}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class BadRestRequestException extends IllegalArgumentException {
    private static final long serialVersionUID = 6852720596988243487L;

    /**
     * Ctor.
     *
     * @param msg error message
     */
    public BadRestRequestException(final String msg) {
        super(msg);
    }
}
