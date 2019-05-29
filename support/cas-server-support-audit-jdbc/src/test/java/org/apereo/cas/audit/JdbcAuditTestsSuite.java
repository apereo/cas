package org.apereo.cas.audit;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link JdbcAuditTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SelectClasses({
    CasSupportJdbcAuditConfigurationTests.class,
    CasSupportMicrosoftSqlServerJdbcAuditConfigurationTests.class,
    CasSupportMySQLJdbcAuditConfigurationTests.class,
    CasSupportPostgresJdbcAuditConfigurationTests.class
})
@RunWith(JUnitPlatform.class)
public class JdbcAuditTestsSuite {
}
