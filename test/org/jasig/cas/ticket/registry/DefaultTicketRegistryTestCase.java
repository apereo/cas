package org.jasig.cas.ticket.registry;


/**
 * Test case to test the DefaultTicketRegistry based on test cases
 * to test all Ticket Registries.
 * 
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public class DefaultTicketRegistryTestCase extends AbstractTicketRegistryTestCase {

	/**
	 * @see org.jasig.cas.ticket.registry.AbstractTicketRegistryTestCase#getNewTicketRegistry()
	 */
	public TicketRegistry getNewTicketRegistry() throws Exception {
		return new DefaultTicketRegistry();
	}
}
