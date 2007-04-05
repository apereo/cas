/**
 * 
 * @author Andres March
 * @version $Revision$ $Date$
 * @since 3.1
 *
 */
package org.jasig.cas.ticket.registry;

import java.io.File;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;

import org.apache.commons.lang.RandomStringUtils;
import org.jasig.cas.authentication.ImmutableAuthentication;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.registry.BerkeleyDbTicketRegistry;
import org.jasig.cas.ticket.support.TimeoutExpirationPolicy;

import com.clarkware.junitperf.LoadTest;

/**
 * 
 * @author Andres March
 * @version $Revision$ $Date$
 * @since 3.1
 *
 */
public class BerkeleyDbTicketRegistryPerformanceTest extends TestCase {

	static BerkeleyDbTicketRegistry registry;

	public BerkeleyDbTicketRegistryPerformanceTest(String name) {
		super(name);
	}

	public static Test suite() {
		BerkeleyDbTicketRegistryPerformanceTest testCase = new BerkeleyDbTicketRegistryPerformanceTest(
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
		String id = RandomStringUtils.randomAlphanumeric(20);
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
