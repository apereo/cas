package org.apereo.cas.services;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllJpaServiceRegistryTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SelectClasses({
    JpaServiceRegistryTests.class,
    JpaServiceRegistryMySQLTests.class,
    JpaServiceRegistryMicrosoftSqlServerTests.class,
    JpaServiceRegistryPostgresTests.class,
    JpaServiceRegistryMariaDbTests.class,
    OidcJpaServiceRegistryTests.class,
    JpaServiceRegistryOracleTests.class
})
@RunWith(JUnitPlatform.class)
public class AllJpaServiceRegistryTestsSuite {
}
