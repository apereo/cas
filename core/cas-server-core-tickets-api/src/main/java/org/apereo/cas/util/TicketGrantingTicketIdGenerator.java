package org.apereo.cas.util;

/**
 * This is {@link TicketGrantingTicketIdGenerator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class TicketGrantingTicketIdGenerator extends HostNameBasedUniqueTicketIdGenerator {

    /**
     * Instantiates a new Ticket granting ticket id generator.
     *
     * @param maxLength the max length
     * @param suffix    the suffix
     */
    public TicketGrantingTicketIdGenerator(final int maxLength,
                                           final String suffix) {
        super(maxLength, suffix);
    }
}
