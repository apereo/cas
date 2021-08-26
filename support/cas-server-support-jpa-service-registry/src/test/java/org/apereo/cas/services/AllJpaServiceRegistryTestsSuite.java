package org.apereo.cas.services;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

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
    JpaServiceRegistryOidcTests.class,
    JpaServiceRegistryOracleTests.class
})
@Suite
public class AllJpaServiceRegistryTestsSuite {
}
