/*
 * Copyright 2009 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.ticket.registry;

import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Test case to test the DefaultTicketRegistry based on test cases to test all
 * Ticket Registries.
 * 
 * @author <a href="mailto:cyrille@cyrilleleclerc.com">Cyrille Le Clerc</a>
 */
public final class EhCacheTicketRegistryTests extends AbstractTicketRegistryTests {

    private ClassPathXmlApplicationContext applicationContext;

    @Override
    protected void setUp() throws Exception {
        applicationContext = new ClassPathXmlApplicationContext("classpath:ticketRegistry.xml");
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        applicationContext.destroy();
    }

    @Override
    public TicketRegistry getNewTicketRegistry() throws Exception {
        return (TicketRegistry) applicationContext.getBean("ticketRegistry");
    }

    /**
     * Disabled because {@link EhCacheTicketRegistry#getTickets()} returns an
     * {@link UnsupportedOperationException}
     */
    @Override
    public void testGetTicketsIsZero() {
    }

    /**
     * Disabled because {@link EhCacheTicketRegistry#getTickets()} returns an
     * {@link UnsupportedOperationException}
     */
    @Override
    public void testGetTicketsFromRegistryEqualToTicketsAdded() {
    }

    /**
     * Disabled because {@link EhCacheTicketRegistry} relies on
     * {@link TicketGrantingTicket#PREFIX} and {@link ServiceTicket#PREFIX} to
     * route getTicket() search on the underlying cache.
     */
    @Override
    public void testGetExistingTicketWithInproperClass() {
    }
}