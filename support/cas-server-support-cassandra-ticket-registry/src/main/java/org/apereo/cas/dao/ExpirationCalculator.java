package org.apereo.cas.dao;

import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;

import static org.apereo.cas.authentication.RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME;

public class ExpirationCalculator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExpirationCalculator.class);
    private long ttl;
    private long ttk;
    private long rememberMeTtl;

    public ExpirationCalculator(final long ttl, final long ttk, final long rememberMeTtl) {
        this.ttl = ttl;
        this.ttk = ttk;
        this.rememberMeTtl = rememberMeTtl;
    }

    public long getExpiration(final TicketGrantingTicketImpl ticket) {
        final Boolean b = (Boolean) ticket.getAuthentication().getAttributes().getOrDefault(AUTHENTICATION_ATTRIBUTE_REMEMBER_ME, false);
        if (b) {
            final long expiry = ticket.getCreationTime().plusSeconds(rememberMeTtl).toEpochSecond();
            LOGGER.debug("Ticket creation time: {}; Ticket expiry: {}", ticket.getCreationTime(), expiry);
            return expiry;
        } else {
            final ZonedDateTime ticketTtl = ticket.getCreationTime().plusSeconds(ttl);
            final ZonedDateTime ticketTtk = ticket.getLastTimeUsed().plusSeconds(ttk);

            final long expiry = ticketTtl.isBefore(ticketTtk) ? ticketTtl.toEpochSecond() : ticketTtk.toEpochSecond();
            LOGGER.debug("Ticket creation time: {}; Ticket expiry: {}", ticket.getCreationTime(), expiry);
            return expiry;
        }
    }
}
