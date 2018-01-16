package org.apereo.cas;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.monitor.SessionHealthIndicatorJpaTests;
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
@Suite.SuiteClasses({SessionHealthIndicatorJpaTests.class, JpaTicketRegistryTests.class,
        JpaLockingStrategyTests.class})
@Slf4j
public class AllTestsSuite {
}
