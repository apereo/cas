/*
 * Copyright 2012 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.jasig.org/cas/license.
 */
package org.jasig.cas.monitor;

/**
 * Describes important state information that may be optionally exposed by
 * {@link org.jasig.cas.ticket.registry.TicketRegistry} components that might
 * be of interest to monitors.
 *
 * @author Marvin S. Addison
 * @version $Revision: $
 */
public interface TicketRegistryState {
    /**
     * Computes the number of SSO sessions stored in the ticket registry.
     *
     * @return Number of ticket-granting tickets in the registry at time of invocation.
     */
    int sessionCount();


     /**
     * Computes the number of service tickets stored in the ticket registry.
     *
     * @return Number of service tickets in the registry at time of invocation.
     */
    int serviceTicketCount();
}
