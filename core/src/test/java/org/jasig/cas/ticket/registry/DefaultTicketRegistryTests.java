/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.registry;

/**
 * Test case to test the DefaultTicketRegistry based on test cases to test all
 * Ticket Registries.
 * 
 * @author Scott Battaglia
 * @version $Id: DefaultTicketRegistryTests.java,v 1.1 2005/02/15 05:06:39
 * sbattaglia Exp $
 */
public class DefaultTicketRegistryTests extends AbstractTicketRegistryTests {

    /**
     * @see org.jasig.cas.ticket.registry.AbstractTicketRegistryTests#getNewTicketRegistry()
     */
    public TicketRegistry getNewTicketRegistry() throws Exception {
        return new DefaultTicketRegistry();
    }
}