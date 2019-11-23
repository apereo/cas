package org.apereo.cas.pm.jdbc;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

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
@RunWith(JUnitPlatform.class)
public class AllJdbcPasswordManagementTestsSuite {
}
