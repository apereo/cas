package org.jasig.cas.helper;

import org.jasig.cas.domain.CasAttributes;
import org.jasig.cas.domain.Principal;
import org.jasig.cas.domain.ProxyGrantingTicket;
import org.jasig.cas.domain.ServiceTicket;
import org.jasig.cas.domain.TicketCreationException;
import org.jasig.cas.domain.support.ServiceTicketImpl;
import org.jasig.cas.domain.support.SimplePrincipal;
import org.jasig.cas.domain.support.TicketGrantingTicketImpl;
import org.jasig.cas.helper.support.DefaultUniqueTicketIdGenerator;
import org.jasig.cas.helper.support.ProxyGrantingTicketCreator;
import org.jasig.cas.strategy.TimeoutExpirationPolicy;

import junit.framework.TestCase;


/**
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public class ProxyGrantingTicketCreatorTest extends TestCase {
	private ProxyGrantingTicketCreator ticketCreator = new ProxyGrantingTicketCreator();
	private CasAttributes casAttributes;
	private final Principal principal = new SimplePrincipal("netId"); 
	
	public ProxyGrantingTicketCreatorTest() {
		ticketCreator.setExpirationPolicy(new TimeoutExpirationPolicy(1000));
		ticketCreator.setUniqueTicketIdGenerator(new DefaultUniqueTicketIdGenerator());
	}
	
	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		casAttributes = new CasAttributes();
	}
	
	public void testNoParameters() {
		try {
			ticketCreator.createTicket(principal, casAttributes, null, null);
		}
		catch (TicketCreationException e) {
			return;
		}
		catch(IllegalArgumentException e) {
			return;
		}
		
		fail("Exception expected");
	}
	
	public void testProperParameters() {
		casAttributes.setCallbackUrl("https://www.acs.rutgers.edu");
		
		ServiceTicket serviceTicket = new ServiceTicketImpl("test1", new TicketGrantingTicketImpl("test2", new SimplePrincipal("user"), new TimeoutExpirationPolicy(1000)), "service", true, new TimeoutExpirationPolicy(1000));
		ProxyGrantingTicket pgt = (ProxyGrantingTicket) ticketCreator.createTicket(principal, casAttributes, "test", serviceTicket);
		
		assertEquals(pgt.getProxyId().toString(), casAttributes.getCallbackUrl());
	}
	
	public void testNonHttpsParameters() {
		casAttributes.setCallbackUrl("http://www.acs.rutgers.edu");
		
		ServiceTicket serviceTicket = new ServiceTicketImpl("test1", new TicketGrantingTicketImpl("test2", new SimplePrincipal("user"), new TimeoutExpirationPolicy(1000)), "service", true, new TimeoutExpirationPolicy(1000));
		try {
			ProxyGrantingTicket pgt = (ProxyGrantingTicket) ticketCreator.createTicket(principal, casAttributes, "test", serviceTicket);
		} catch (IllegalArgumentException e) {
			return;
		}
		
		fail("IllegalArgumentException expected for non https");
	}
	
	public void testBadUrlParameters() {
		casAttributes.setCallbackUrl("aaaa");
		
		ServiceTicket serviceTicket = new ServiceTicketImpl("test1", new TicketGrantingTicketImpl("test2", new SimplePrincipal("user"), new TimeoutExpirationPolicy(1000)), "service", true, new TimeoutExpirationPolicy(1000));
		try {
			ProxyGrantingTicket pgt = (ProxyGrantingTicket) ticketCreator.createTicket(principal, casAttributes, "test", serviceTicket);
		} catch (TicketCreationException e) {
			return;
		}
		
		fail("TicketCreationException expected.");
	}
}
