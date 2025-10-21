package org.apereo.cas.audit;

import org.apereo.cas.util.junit.EnabledIfListeningOnPort;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link MicrosoftSqlServerJdbcAuditConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@TestPropertySource(properties = {
    "cas.audit.jdbc.user=sa",
    "cas.audit.jdbc.password=p@ssw0rd",
    "cas.audit.jdbc.driver-class=com.microsoft.sqlserver.jdbc.SQLServerDriver",
    "cas.audit.jdbc.url=jdbc:sqlserver://localhost:1433;databaseName=audit;encrypt=false;trustServerCertificate=true",
    "cas.audit.jdbc.dialect=org.hibernate.dialect.SQLServerDialect"
    })
@EnabledIfListeningOnPort(port = 1433)
@Tag("MsSqlServer")
class MicrosoftSqlServerJdbcAuditConfigurationTests extends CasJdbcAuditConfigurationTests {
}
