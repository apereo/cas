package org.jasig.cas.helper;

import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.ticket.CasAttributes;
import org.jasig.cas.ticket.ProxyGrantingTicket;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.ServiceTicketImpl;
import org.jasig.cas.ticket.TicketCreationException;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.factory.support.ProxyGrantingTicketCreator;
import org.jasig.cas.ticket.support.TimeoutExpirationPolicy;
import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Id$
 */
public class ProxyGrantingTicketCreatorTest extends TestCase {

    private ProxyGrantingTicketCreator ticketCreator = new ProxyGrantingTicketCreator();

    private CasAttributes casAttributes;

    private final Principal principal = new SimplePrincipal("netId");

    public ProxyGrantingTicketCreatorTest() {
        this.ticketCreator.setExpirationPolicy(new TimeoutExpirationPolicy(1000));
        this.ticketCreator.setUniqueTicketIdGenerator(new DefaultUniqueTicketIdGenerator());
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        this.casAttributes = new CasAttributes();
    }

    public void testNoParameters() {
        try {
            this.ticketCreator.createTicket(this.principal, this.casAttributes, null);
        }
        catch (TicketCreationException e) {
            return;
        }
        catch (IllegalArgumentException e) {
            return;
        }

        fail("Exception expected");
    }

    public void testProperParameters() {
        this.casAttributes.setCallbackUrl("https://www.acs.rutgers.edu");

        ServiceTicket serviceTicket = new ServiceTicketImpl("test1", new TicketGrantingTicketImpl("test2", new SimplePrincipal("user"),
            new TimeoutExpirationPolicy(1000)), "service", true, new TimeoutExpirationPolicy(1000));
        ProxyGrantingTicket pgt = (ProxyGrantingTicket)this.ticketCreator.createTicket(this.principal, this.casAttributes, serviceTicket);

        assertEquals(pgt.getProxyId().toString(), this.casAttributes.getCallbackUrl());
    }

    public void testNonHttpsParameters() {
        this.casAttributes.setCallbackUrl("http://www.acs.rutgers.edu");

        ServiceTicket serviceTicket = new ServiceTicketImpl("test1", new TicketGrantingTicketImpl("test2", new SimplePrincipal("user"),
            new TimeoutExpirationPolicy(1000)), "service", true, new TimeoutExpirationPolicy(1000));
        try {
            ProxyGrantingTicket pgt = (ProxyGrantingTicket)this.ticketCreator.createTicket(this.principal, this.casAttributes, serviceTicket);
        }
        catch (IllegalArgumentException e) {
            return;
        }

        fail("IllegalArgumentException expected for non https");
    }

    public void testBadUrlParameters() {
        this.casAttributes.setCallbackUrl("aaaa");

        ServiceTicket serviceTicket = new ServiceTicketImpl("test1", new TicketGrantingTicketImpl("test2", new SimplePrincipal("user"),
            new TimeoutExpirationPolicy(1000)), "service", true, new TimeoutExpirationPolicy(1000));
        try {
            ProxyGrantingTicket pgt = (ProxyGrantingTicket) this.ticketCreator.createTicket(this.principal, this.casAttributes,  serviceTicket);
        }
        catch (TicketCreationException e) {
            return;
        }

        fail("TicketCreationException expected.");
    }
}
