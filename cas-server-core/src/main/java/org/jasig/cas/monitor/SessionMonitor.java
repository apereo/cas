/*
 * Copyright 2012 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.jasig.org/cas/license.
 */
package org.jasig.cas.monitor;

import javax.validation.constraints.NotNull;

/**
 * Monitors the status of a {@link org.jasig.cas.ticket.registry.TicketRegistry}
 * that supports the {@link TicketRegistryState} interface for exposing internal
 * state information used in status reports.
 *
 * @author Marvin S. Addison
 * @version $Revision: $
 */
public class SessionMonitor implements Monitor<SessionStatus> {
    /** Ticket registry instance that exposes state info. */
    @NotNull
    private TicketRegistryState registryState;

    /** Threshold above which warnings are issued for session count. */
    private int sessionCountWarnThreshold = -1;

    /** Threshold above which warnings are issued for service ticket count. */
    private int serviceTicketCountWarnThreshold = -1;


    /**
     * Sets the ticket registry that exposes state information that may be queried by this monitor.
     * @param state
     */
    public void setTicketRegistry(final TicketRegistryState state) {
        this.registryState = state;
    }


    /**
     * Sets the threshold above which warnings are issued for session counts in excess of value.
     *
     * @param threshold Warn threshold if non-negative value, otherwise warnings are disabled.
     */
    public void setSessionCountWarnThreshold(final int threshold) {
        this.sessionCountWarnThreshold = threshold;
    }


    /**
     * Sets the threshold above which warnings are issued for service ticket counts in excess of value.
     *
     * @param threshold Warn threshold if non-negative value, otherwise warnings are disabled.
     */
    public void setServiceTicketCountWarnThreshold(final int threshold) {
        this.serviceTicketCountWarnThreshold = threshold;
    }


    /** {@inheritDoc} */
    public String getName() {
        return SessionMonitor.class.getSimpleName();
    }


    /** {@inheritDoc} */
    public SessionStatus observe() {
        try {
            final int sessionCount = registryState.sessionCount();
            final int ticketCount = registryState.serviceTicketCount();
            final StringBuilder msg = new StringBuilder();
            StatusCode code = StatusCode.OK;
            if (sessionCountWarnThreshold > -1 && sessionCount > sessionCountWarnThreshold) {
                code = StatusCode.WARN;
                msg.append(String.format(
                        "Session count (%s) is above threshold %s. ", sessionCount, sessionCountWarnThreshold));
            } else {
                msg.append(sessionCount).append(" sessions. ");
            }
            if (serviceTicketCountWarnThreshold > -1 && ticketCount > serviceTicketCountWarnThreshold) {
                code = StatusCode.WARN;
                msg.append(String.format(
                        "Service ticket count (%s) is above threshold %s.",
                        ticketCount,
                        serviceTicketCountWarnThreshold));
            } else {
                msg.append(ticketCount).append(" service tickets.");
            }
            return new SessionStatus(code, msg.toString(), sessionCount, ticketCount);
        } catch (Exception e) {
            return new SessionStatus(StatusCode.ERROR, e.getMessage());
        }
    }
}
