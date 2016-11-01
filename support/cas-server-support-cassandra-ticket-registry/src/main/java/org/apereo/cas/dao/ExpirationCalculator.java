package org.apereo.cas.dao;

import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

import static org.apereo.cas.authentication.RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME;

@Component
public class ExpirationCalculator {

    private static final Logger LOG = LoggerFactory.getLogger(ExpirationCalculator.class);
    private long ttl;
    private long ttk;
    private long rememberMeTtl;

    @Autowired
    public ExpirationCalculator(@Value("${tgt.maxTimeToLiveInSeconds:28800}") long ttl, @Value("${tgt.timeToKillInSeconds:7200}") long ttk, @Value("${tgt.maxRememberMeTimeoutExpiration}") long rememberMeTtl) {
        this.ttl = ttl;
        this.ttk = ttk;
        this.rememberMeTtl = rememberMeTtl;
    }

    public long getExpiration(TicketGrantingTicketImpl ticket) {
        final Boolean b = (Boolean) ticket.getAuthentication().getAttributes().get(AUTHENTICATION_ATTRIBUTE_REMEMBER_ME);
        if (b != null && b) {
            long expiry = ticket.getCreationTime().plusSeconds(rememberMeTtl).toEpochSecond();
            LOG.debug("Ticket creation time: {}; Ticket expiry: {}",  ticket.getCreationTime(), expiry); 
            return expiry;
        } else {
            ZonedDateTime ticketTtl = ticket.getCreationTime().plusSeconds(ttl);
            ZonedDateTime ticketTtk = ticket.getLastTimeUsed().plusSeconds(ttk);
            long expiry = ticketTtl.isBefore(ticketTtk) ? ticketTtl.toEpochSecond() : ticketTtk.toEpochSecond();
            LOG.debug("Ticket creation time: {}; Ticket expiry: {}",  ticket.getCreationTime(), expiry);
            return expiry;
        }
    }
}
