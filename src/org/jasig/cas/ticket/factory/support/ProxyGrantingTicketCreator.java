/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.factory.support;

import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.ticket.CasAttributes;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.ProxyGrantingTicket;
import org.jasig.cas.ticket.ProxyGrantingTicketImpl;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketCreationException;
import org.jasig.cas.ticket.factory.TicketCreator;
import org.jasig.cas.util.UniqueTicketIdGenerator;

/**
 * TicketCreator for ProxyGrantingTicket
 * 
 * @author Scott Battaglia
 * @version $Id$
 * @see ProxyGrantingTicketCreator
 */
public class ProxyGrantingTicketCreator implements TicketCreator {

    protected final Log log = LogFactory.getLog(getClass());

    private static final String PREFIX = "PGT";

    private static final String PGTIOU_PREFIX = "PGTIOU";

    private ExpirationPolicy expirationPolicy;

    private UniqueTicketIdGenerator uniqueTicketIdGenerator;

    /**
     * @see org.jasig.cas.ticket.factory.TicketCreator#createTicket(org.jasig.cas.authentication.principal.Principal,
     * org.jasig.cas.ticket.CasAttributes, java.lang.String, org.jasig.cas.ticket.Ticket)
     */
    public Ticket createTicket(final Principal principal, final CasAttributes casAttributes, final String ticketId, final Ticket grantingTicket) {
        String pgtIou = this.uniqueTicketIdGenerator.getNewTicketId(PGTIOU_PREFIX);

        log.debug("Creating ticket of type ProxyGrantingTicket with ID [" + ticketId + "]");

        try {
            ProxyGrantingTicket ticket = new ProxyGrantingTicketImpl(ticketId, (ServiceTicket)grantingTicket,
                new URL(casAttributes.getCallbackUrl()), pgtIou, this.expirationPolicy);
            return ticket;
        }
        catch (MalformedURLException mue) {
            throw new TicketCreationException("Unable to create ticket with callbackUrl of " + casAttributes.getCallbackUrl());
        }
    }

    /**
     * @see org.jasig.cas.ticket.factory.TicketCreator#supports(java.lang.Class)
     */
    public boolean supports(final Class clazz) {
        return ProxyGrantingTicket.class.equals(clazz);
    }

    /**
     * @see org.jasig.cas.ticket.factory.TicketCreator#getPrefix()
     */
    public String getPrefix() {
        return PREFIX;
    }

    /**
     * @param expirationPolicy The expirationPolicy to set.
     */
    public void setExpirationPolicy(final ExpirationPolicy expirationPolicy) {
        this.expirationPolicy = expirationPolicy;
    }

    /**
     * @param uniqueTicketIdGenerator The uniqueTicketIdGenerator to set.
     */
    public void setUniqueTicketIdGenerator(final UniqueTicketIdGenerator uniqueTicketIdGenerator) {
        this.uniqueTicketIdGenerator = uniqueTicketIdGenerator;
    }
}