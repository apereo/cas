package org.apereo.cas.audit;

import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSupportMicrosoftSqlServerJdbcAuditConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@TestPropertySource(properties = {
    "cas.audit.jdbc.user=sa",
    "cas.audit.jdbc.password=p@ssw0rd",
    "cas.audit.jdbc.driverClass=com.microsoft.sqlserver.jdbc.SQLServerDriver",
    "cas.audit.jdbc.url=jdbc:sqlserver://localhost:1433;databaseName=audit",
    "cas.audit.jdbc.dialect=org.hibernate.dialect.SQLServer2012Dialect"
    })
@EnabledIfPortOpen(port = 1433)
@Tag("MsSqlServer")
public class CasSupportMicrosoftSqlServerJdbcAuditConfigurationTests extends CasSupportJdbcAuditConfigurationTests {
}
