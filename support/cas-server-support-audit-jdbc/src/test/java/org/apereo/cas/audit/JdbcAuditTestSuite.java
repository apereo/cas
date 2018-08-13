package org.apereo.cas.audit;

import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link JdbcAuditTestSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RunWith(Enclosed.class)
@Suite.SuiteClasses({
    CasSupportJdbcAuditConfigurationTests.class,
    CasSupportMicrosoftSqlServerJdbcAuditConfigurationTests.class,
    CasSupportMySQLJdbcAuditConfigurationTests.class,
    CasSupportPostgresJdbcAuditConfigurationTests.class
})
public class JdbcAuditTestSuite {
}
