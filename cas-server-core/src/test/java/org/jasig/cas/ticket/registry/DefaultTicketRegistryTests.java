/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.ticket.registry;

/**
 * Test case to test the DefaultTicketRegistry based on test cases to test all
 * Ticket Registries.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class DefaultTicketRegistryTests extends AbstractTicketRegistryTests {

    public TicketRegistry getNewTicketRegistry() throws Exception {
        return new DefaultTicketRegistry();
    }
    
    public void testOtherConstructor() {
        assertNotNull(new DefaultTicketRegistry(10, 10F, 5));
    }
}