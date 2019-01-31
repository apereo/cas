package org.apereo.cas.audit;

import org.junit.platform.suite.api.SelectClasses;

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
public class JdbcAuditTestsSuite {
}
