package org.apereo.cas;

import org.apereo.cas.monitor.SessionHealthIndicatorJpaTests;
import org.apereo.cas.ticket.registry.JpaTicketRegistryCleanerTests;
import org.apereo.cas.ticket.registry.JpaTicketRegistryTests;
import org.apereo.cas.ticket.registry.MySQLJpaTicketRegistryTests;
import org.apereo.cas.ticket.registry.OAuthJpaTicketRegistryCleanerTests;
import org.apereo.cas.ticket.registry.OAuthJpaTicketRegistryTests;
import org.apereo.cas.ticket.registry.OracleJpaTicketRegistryTests;
import org.apereo.cas.ticket.registry.PostgresOauthJpaTicketRegistryCleanerTests;
import org.apereo.cas.ticket.registry.support.JpaLockingStrategyTests;
import org.apereo.cas.ticket.registry.support.OracleJpaLockingStrategyTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@SelectClasses({
    SessionHealthIndicatorJpaTests.class,
    JpaTicketRegistryTests.class,
    JpaLockingStrategyTests.class,
    MySQLJpaTicketRegistryTests.class,
    JpaTicketRegistryCleanerTests.class,
    OAuthJpaTicketRegistryCleanerTests.class,
    OAuthJpaTicketRegistryTests.class,
    PostgresOauthJpaTicketRegistryCleanerTests.class,
    OracleJpaTicketRegistryTests.class,
    OracleJpaLockingStrategyTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
