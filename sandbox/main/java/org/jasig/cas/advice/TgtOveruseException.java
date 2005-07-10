/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.advice;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.TicketValidationException;

/**
 * Exception indicating failure to validate a potentially otherwise valid ticket
 * granting ticket id because it has been overused.
 */
public class TgtOveruseException extends TicketValidationException {

    private static final long serialVersionUID = -1931457301226439078L;

    /**
     * Non-null String ticket granting ticket identifier which was overused,
     * prompting this exception.
     */
    private final String overusedTgtId;

    /**
     * The TGT was being presented to obtain a Service Ticket targetted at this
     * service when the TGT was discovered to be overused, or null if no target
     * Service.
     */
    private final Service targetService;

    public TgtOveruseException(String overusedTgtId) {
        // delegate to the all-field constructor
        this(overusedTgtId, null);
    }

    public TgtOveruseException(String overusedTgtId, Service targetService) {

        if (overusedTgtId == null) {
            throw new IllegalArgumentException(
                "Cannot overuse a null ticket granting ticket identifier.");
        }

        this.overusedTgtId = overusedTgtId;

        this.targetService = targetService;
    }

    /**
     * Returns the String representing the tgtid that was overused, prompting
     * this exception. Care should be taken in how this String is handled, as it
     * is the shared secret between CAS server and and end user or a
     * proxy-CAS-using service for participating in a CAS Single Sign On
     * session.
     * 
     * @return non-null overused String identifier for a ticket granting ticket.
     */
    public String getOverusedTgtId() {
        return this.overusedTgtId;
    }

    /**
     * Returns the Service for which a ServiceTicket was requested at the time
     * the TGT was discovered to be overused, or null if no such Service. The
     * Service can be instructive as to the cause of the over-use, e.g. if the
     * Service has a broken usage of CAS.
     * 
     * @return the target Service.
     */
    public Service getTargetService() {
        return this.targetService;
    }

}
