package org.apereo.cas.ticket;

/**
 * This is {@link EncodedTicket}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public interface EncodedTicket extends Ticket {

    /**
     * Get encoded ticket as byte [].
     *
     * @return the byte []
     */
    byte[] getEncodedTicket();
}
