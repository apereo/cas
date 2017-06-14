package org.apereo.cas.support.rest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({TicketGrantingTicketResourceTests.class, ServiceTicketResourceTests.class})
public class AllTestsSuite {
}
