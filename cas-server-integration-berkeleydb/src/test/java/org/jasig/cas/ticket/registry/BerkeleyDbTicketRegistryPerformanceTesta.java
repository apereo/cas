/**
 * 
 * @author Andres March
 * @version $Revision$ $Date$
 * @since 3.1
 *
 */

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

import java.io.File;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;

import org.jasig.cas.authentication.ImmutableAuthentication;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.registry.BerkeleyDbTicketRegistry;
import org.jasig.cas.ticket.support.TimeoutExpirationPolicy;
import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;
import org.jasig.cas.util.UniqueTicketIdGenerator;

import com.clarkware.junitperf.LoadTest;

/**
 * 
 * @author Andres March
 * @version $Revision$ $Date$
 * @since 3.1
 *
 */
public class BerkeleyDbTicketRegistryPerformanceTesta extends TestCase {

	static BerkeleyDbTicketRegistry registry;
	
	private final UniqueTicketIdGenerator generator = new DefaultUniqueTicketIdGenerator();

	public BerkeleyDbTicketRegistryPerformanceTesta(String name) {
		super(name);
	}

	public static Test suite() {
		BerkeleyDbTicketRegistryPerformanceTesta testCase = new BerkeleyDbTicketRegistryPerformanceTesta(
				"testAddGetRemove");


		return new BerkleyDbTicketRegistryTestSetup(new LoadTest(testCase,
				10));
	}

	public void testAddGetRemove() throws Exception {
		int numTimes = 100;
		for (int i = 0; i < numTimes; i++) {
			Ticket originalTicket = generateRandomTicket();
			String id = originalTicket.getId();

			registry.addTicket(originalTicket);

			assertEquals(originalTicket, registry.getTicket(id));

			assertTrue(registry.deleteTicket(id));
		}

	}

	private TicketGrantingTicketImpl generateRandomTicket() {
		String id = this.generator.getNewTicketId("TGT");
        final SimplePrincipal principal = new SimplePrincipal(id);
		TicketGrantingTicketImpl ticket = new TicketGrantingTicketImpl(id,
				new ImmutableAuthentication(principal),
				new TimeoutExpirationPolicy(500));

		return ticket;
	}

	private static class BerkleyDbTicketRegistryTestSetup extends TestSetup {

		public BerkleyDbTicketRegistryTestSetup(Test test) {
			super(test);
		}

		protected void setUp() throws Exception {
			super.setUp();
			registry = new BerkeleyDbTicketRegistry();
			registry.afterPropertiesSet();
		}

		protected void tearDown() throws Exception {
			registry.destroy();
			registry = null;
			new File("00000000.jdb").delete();
			new File("je.lck").delete();
			super.tearDown();
		}
	}
}
