package org.apereo.cas.services;

import org.junit.platform.suite.api.SelectClasses;

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
    JpaServiceRegistryMariaDbTests.class
})
public class AllJpaServiceRegistryTestsSuite {
}
