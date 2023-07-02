package org.apereo.cas.audit;

import org.apereo.cas.util.junit.EnabledIfListeningOnPort;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link OracleJdbcAuditConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@TestPropertySource(properties = {
    "cas.jdbc.show-sql=false",
    "cas.audit.jdbc.ddl-auto=create-drop",
    "cas.audit.jdbc.date-formatter-function=TO_DATE('%s', '%s')",
    "cas.audit.jdbc.date-formatter-pattern=yyyy-MM-dd",
    "cas.audit.jdbc.user=system",
    "cas.audit.jdbc.password=Oradoc_db1",
    "cas.audit.jdbc.driver-class=oracle.jdbc.driver.OracleDriver",
    "cas.audit.jdbc.url=jdbc:oracle:thin:@localhost:1521:ORCLCDB",
    "cas.audit.jdbc.dialect=org.hibernate.dialect.OracleDialect"
})
@EnabledIfListeningOnPort(port = 1521)
@Tag("Oracle")
class OracleJdbcAuditConfigurationTests extends CasJdbcAuditConfigurationTests {
}
