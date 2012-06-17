/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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

    public EhCacheTicketRegistryTests() {
        applicationContext = new ClassPathXmlApplicationContext("classpath:ticketRegistry.xml");
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

}