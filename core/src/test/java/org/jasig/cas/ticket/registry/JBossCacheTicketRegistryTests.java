/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.registry;

import org.jboss.cache.TreeCache;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Test case to test the DefaultTicketRegistry based on test cases to test all
 * Ticket Registries.
 * 
 * @author Scott Battaglia
 * @author Marc-Antoine Garrigue
 * @version $Revision$ $Date$
 */
public final class JBossCacheTicketRegistryTests extends AbstractTicketRegistryTests {

    private static final String APPLICATION_CONTEXT_FILE_NAME = "jbossTestContext.xml";

    private static final String APPLICATION_CONTEXT_CACHE_BEAN_NAME = "ticketRegistry";

    private JBossCacheTicketRegistry registry;
    
    private TreeCache treeCache;

    public TicketRegistry getNewTicketRegistry() throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
            APPLICATION_CONTEXT_FILE_NAME);
        this.registry = (JBossCacheTicketRegistry) context
            .getBean(APPLICATION_CONTEXT_CACHE_BEAN_NAME);

        this.treeCache = (TreeCache) context.getBean("cache");
        this.treeCache.removeData("/ticket");
        
        return this.registry;
    }
}