/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.registry.support;

import org.jasig.cas.ticket.registry.AbstractRegistryCleanerTestCase;
import org.jasig.cas.ticket.registry.DefaultTicketRegistry;
import org.jasig.cas.ticket.registry.RegistryCleaner;
import org.jasig.cas.ticket.registry.TicketRegistry;

/**
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public class DefaultTicketRegistryCleanerTestCase extends AbstractRegistryCleanerTestCase {
    /**
     * @see org.jasig.cas.ticket.registry.AbstractTicketRegistryTestCase#getNewTicketRegistry()
     */
    public RegistryCleaner getNewRegistryCleaner(final TicketRegistry ticketRegistry) {
        DefaultTicketRegistryCleaner cleaner = new DefaultTicketRegistryCleaner();
        cleaner.setTicketRegistry(ticketRegistry);
        
        return cleaner;
    }
    
    public TicketRegistry getNewTicketRegistry() {
        return new DefaultTicketRegistry();
    }
}
