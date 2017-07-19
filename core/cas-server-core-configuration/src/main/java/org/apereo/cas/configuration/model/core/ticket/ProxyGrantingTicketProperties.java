package org.apereo.cas.configuration.model.core.ticket;

/**
 * This is {@link ProxyGrantingTicketProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class ProxyGrantingTicketProperties {
    /**
     * Maximum length of the proxy granting ticket, when generating one.
     */
    private int maxLength = 50;

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(final int maxLength) {
        this.maxLength = maxLength;
    }
}
