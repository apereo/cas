package org.apereo.cas.adaptors.yubikey.dao;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllJpaYubiKeyTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SelectClasses({
    JpaYubiKeyAccountRegistryTests.class,
    MicrosoftSQLServerJpaYubiKeyAccountRegistryTests.class,
    MySQLJpaYubiKeyAccountRegistryTests.class,
    PostgresJpaYubiKeyAccountRegistryTests.class
})
@Suite
public class AllJpaYubiKeyTestsSuite {
}
