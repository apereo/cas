package org.apereo.cas.configuration.model.core.ticket;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link TicketGrantingTicketCoreProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-core-tickets", automated = true)
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("TicketGrantingTicketCoreProperties")
public class TicketGrantingTicketCoreProperties implements Serializable {

    private static final long serialVersionUID = 2349179252583399336L;

    /**
     * Maximum length of tickets.
     */
    private int maxLength = 50;

    /**
     * Flag to control whether to track most recent SSO sessions.
     * As multiple tickets may be issued for the same application, this impacts
     * how session information is tracked for every ticket which then
     * has a subsequent impact on logout.
     */
    private boolean onlyTrackMostRecentSession = true;
}
