package org.apereo.cas.services;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link AllJpaServiceRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    JpaServiceRegistryTests.class,
    JpaServiceRegistryMySQLTests.class,
    JpaServiceRegistryMicrosoftSqlServerTests.class,
    JpaServiceRegistryPostgresTests.class
})
public class AllJpaServiceRegistryTests {
}
