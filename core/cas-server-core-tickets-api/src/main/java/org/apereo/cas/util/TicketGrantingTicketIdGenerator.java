package org.apereo.cas.util;
import module java.base;

/**
 * This is {@link TicketGrantingTicketIdGenerator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class TicketGrantingTicketIdGenerator extends HostNameBasedUniqueTicketIdGenerator {

    public TicketGrantingTicketIdGenerator(final int maxLength,
                                           final String suffix) {
        super(maxLength, suffix);
    }
}
