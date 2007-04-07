/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.ticket.registry.support;

import org.jasig.cas.ticket.registry.AbstractRegistryCleanerTests;
import org.jasig.cas.ticket.registry.DefaultTicketRegistry;
import org.jasig.cas.ticket.registry.RegistryCleaner;
import org.jasig.cas.ticket.registry.TicketRegistry;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class DefaultTicketRegistryCleanerTests extends
    AbstractRegistryCleanerTests {

    public RegistryCleaner getNewRegistryCleaner(
        final TicketRegistry ticketRegistry) {
        DefaultTicketRegistryCleaner cleaner = new DefaultTicketRegistryCleaner();
        cleaner.setTicketRegistry(ticketRegistry);

        return cleaner;
    }

    public TicketRegistry getNewTicketRegistry() {
        return new DefaultTicketRegistry();
    }
}