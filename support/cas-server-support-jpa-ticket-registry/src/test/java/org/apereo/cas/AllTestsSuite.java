package org.apereo.cas;

import org.apereo.cas.monitor.SessionHealthIndicatorJpaTests;
import org.apereo.cas.ticket.registry.JpaTicketRegistryTests;
import org.apereo.cas.ticket.registry.MySQLJpaTicketRegistryTests;
import org.apereo.cas.ticket.registry.OracleJpaTicketRegistryTests;
import org.apereo.cas.ticket.registry.PostgresJpaTicketRegistryTests;
import org.apereo.cas.ticket.registry.cleaner.MySQLJpaTicketRegistryCleanerTests;
import org.apereo.cas.ticket.registry.cleaner.PostgresJpaTicketRegistryCleanerTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@SelectClasses({
    SessionHealthIndicatorJpaTests.class,
    PostgresJpaTicketRegistryTests.class,
    JpaTicketRegistryTests.class,
    MySQLJpaTicketRegistryCleanerTests.class,
    MySQLJpaTicketRegistryTests.class,
    PostgresJpaTicketRegistryCleanerTests.class,
    OracleJpaTicketRegistryTests.class
})
@Suite
public class AllTestsSuite {
}
