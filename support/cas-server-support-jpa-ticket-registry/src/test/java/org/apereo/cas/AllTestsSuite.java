package org.apereo.cas;

import org.apereo.cas.monitor.SessionMonitorJpaTests;
import org.apereo.cas.ticket.registry.JpaTicketRegistryTests;
import org.apereo.cas.ticket.registry.support.JpaLockingStrategyTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({SessionMonitorJpaTests.class, JpaTicketRegistryTests.class,
        JpaLockingStrategyTests.class})
public class AllTestsSuite {
}
