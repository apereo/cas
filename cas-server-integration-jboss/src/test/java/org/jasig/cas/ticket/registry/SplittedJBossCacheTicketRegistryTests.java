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

import org.jasig.cas.ticket.Ticket;
import org.jboss.cache.Cache;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test case to test the DefaultTicketRegistry based on test cases to test all
 * Ticket Registries. Here two separate {@link Cache}-Instances are configured
 * for ticket granting tickets and service tickets.
 * 
 * @author Odilo Oehmichen, <a href="http://www.swiftmind.com">Swiftmind GmbH</a>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(value="/splittedJbossTestContext.xml")
public class SplittedJBossCacheTicketRegistryTests extends JBossCacheTicketRegistryTests {

    @Autowired  @Qualifier(value="cache")
    private Cache<String, Ticket> serviceTicketTreeCache;
	
    @Before
    public void setUp() throws Exception {
    	super.setUp();
    	serviceTicketTreeCache.removeNode(JBossCacheTicketRegistry.FQN_SERVICE_TICKET);
    }

}
