package org.apereo.cas;

import org.apereo.cas.audit.CasSupportJdbcAuditConfigurationTests;
import org.apereo.cas.audit.CasSupportMicrosoftSqlServerJdbcAuditConfigurationTests;
import org.apereo.cas.audit.CasSupportMySQLJdbcAuditConfigurationTests;
import org.apereo.cas.audit.CasSupportOracleJdbcAuditConfigurationTests;
import org.apereo.cas.audit.CasSupportPostgresJdbcAuditConfigurationTests;

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
    CasSupportOracleJdbcAuditConfigurationTests.class,
    CasSupportMicrosoftSqlServerJdbcAuditConfigurationTests.class,
    CasSupportMySQLJdbcAuditConfigurationTests.class,
    CasSupportPostgresJdbcAuditConfigurationTests.class
})
@RunWith(JUnitPlatform.class)
public class JdbcAuditTestsSuite {
}
