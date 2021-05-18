package org.apereo.cas.pm.jdbc;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllJdbcPasswordManagementTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SelectClasses({
    JdbcPasswordHistoryServiceTests.class,
    MySQLJdbcPasswordHistoryServiceTests.class,
    JdbcPasswordManagementServiceTests.class,
    PostgresJdbcPasswordHistoryServiceTests.class,
    MySQLJdbcPasswordManagementServiceTests.class
})
@Suite
public class AllJdbcPasswordManagementTestsSuite {
}
