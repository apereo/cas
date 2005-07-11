/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.advice;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.TicketException;

/**
 * Exception indicating failure to validate a potentially otherwise valid ticket
 * granting ticket id because it has been overused.
 */
public class TgtOveruseException extends TicketException {

    private static final long serialVersionUID = -1931457301226439078L;

    /**
     * Code we will present to the TicketException superclass and which
     * TicketException will present in answer to getCode(). Codes are mapped to
     * messages, by default in messages.properties.
     */
    public static final String TGT_OVERUSE_CODE = "TGT_OVERUSE";

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
        // configure the TicketException superclass to use the String code
        // appropriate for this exception
        super(TGT_OVERUSE_CODE);

        if (overusedTgtId == null) {
            throw new IllegalArgumentException(
                "Cannot overuse a null ticket granting ticket identifier.");
        }

        this.overusedTgtId = overusedTgtId;

        this.targetService = targetService;
    }

    public String getMessage() {
        if (this.targetService == null) {
            return "The ticket granting ticket ["
                + this.overusedTgtId
                + "] is overused and so was denied in its attempt to obtain a service ticket";
        } else {
            return "The ticket granting ticket ["
                + this.overusedTgtId
                + "] is overused and so was denied in its attempt to obtain a service ticket to access service ["
                + this.targetService + "]";
        }
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
