/**
 * 
 * @author Andres March
 * @version $Revision$ $Date$
 * @since 3.1
 *
 */
package org.jasig.cas.ticket.registry;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import junit.framework.TestCase;

import org.jasig.cas.authentication.ImmutableAuthentication;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.support.TimeoutExpirationPolicy;

/**
 * 
 * @author Andres March
 * @version $Revision$ $Date$
 * @since 3.1
 *
 */
public class BerkeleyDbTicketRegistryTests extends TestCase {

	BerkeleyDbTicketRegistry reg;

	public static final String DEFAULT_ID = "123";

	public static final Random rand = new Random();

	protected void setUp() throws Exception {

		super.setUp();
		this.reg = new BerkeleyDbTicketRegistry();
		this.reg.afterPropertiesSet();
	}
	
	protected void tearDown() throws Exception{
		this.reg.destroy();
		this.reg = null;
		new File("00000000.jdb").delete();
		new File("je.lck").delete();
		super.tearDown();
	}

	public void testAddTicket() throws Exception {

        TicketGrantingTicket ticket = getTicket();
		this.reg.addTicket(ticket);
	}

	TicketGrantingTicket getTicket(String id) {
        final SimplePrincipal principal = new SimplePrincipal(id);
        TicketGrantingTicket ticket = new TicketGrantingTicketImpl(id, new ImmutableAuthentication(principal), new TimeoutExpirationPolicy(500));

		return ticket;
	}

	private TicketGrantingTicket getTicket() {
		return  getTicket(DEFAULT_ID) ;
	}

	public void testGetTicket() throws Exception {

        TicketGrantingTicket oldticket = getTicket();
		long cdate = oldticket.getCreationTime();
		this.reg.addTicket(oldticket);
		Thread.sleep(1000);

		TicketGrantingTicket newticket = (TicketGrantingTicket) this.reg.getTicket(DEFAULT_ID);

		assertEquals(newticket.getCreationTime(), cdate);
		assertEquals(newticket,oldticket);
	}

	public void testGetTickets() throws Exception {

        TicketGrantingTicket ticket = getTicket();
		this.reg.addTicket(ticket);
		ticket = getTicket("abc");
		this.reg.addTicket(ticket);
		Collection tickets = this.reg.getTickets();

		assertNotNull( tickets);
	}

	public void testConcurrency() throws Exception {
		int i = 10;
		List<Thread> threads = new ArrayList<Thread>();
		for (int j = 0; j < i; j++) {
			Runnable runner = new BerkeleyDbTicketRegistryRunner(this);
			Thread thread = new Thread(runner);
			thread.start();
			threads.add(thread);

		}

		for (Thread thread : threads) {
		    thread.join();
		}
	}

	public class BerkeleyDbTicketRegistryRunner implements Runnable {

		BerkeleyDbTicketRegistryTests test;
		long createTime = System.nanoTime();

		public BerkeleyDbTicketRegistryRunner() {
		    // nothing to do
		}

		public BerkeleyDbTicketRegistryRunner(BerkeleyDbTicketRegistryTests test) {
			this.test = test;
		}

		public BerkeleyDbTicketRegistryTests getTest() {
			return this.test;
		}

		public void setTest(BerkeleyDbTicketRegistryTests test) {
			this.test = test;
		}

		public void run() {

		    for (int i = 0; i < 3 ; i++) {
                TicketGrantingTicket ticket = this.test.getTicket(this.toString() + this.createTime);
			this.test.getReg().addTicket(ticket);
            TicketGrantingTicket oldticket = this.test.getTicket(this.toString() + this.createTime);
			BerkeleyDbTicketRegistryTests.this.reg.addTicket(oldticket);
			TicketGrantingTicket newticket = (TicketGrantingTicket) this.test.getReg().getTicket(this.toString() + this.createTime);
			assertEquals(newticket,oldticket);
			boolean delete = (rand.nextInt() % 3) == 0;
			if (!delete) {
			    this.test.getReg().deleteTicket(this.toString() + this.createTime);
			}

			this.test.getReg().getTickets();

		    }


		}

	}

	public BerkeleyDbTicketRegistry getReg() {
		return this.reg;
	}
}
