package org.apereo.cas.audit;

import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSupportOracleJdbcAuditConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@TestPropertySource(properties = {
    "cas.jdbc.show-sql=false",
    "cas.audit.jdbc.ddl-auto=create",
    "cas.audit.jdbc.select-sql-query-template=SELECT * FROM %s WHERE AUD_DATE>=TO_DATE('%s', 'YYYY-MM-DD') ORDER BY AUD_DATE DESC",
    "cas.audit.jdbc.date-formatter-pattern=yyyy-MM-dd",
    "cas.audit.jdbc.user=system",
    "cas.audit.jdbc.password=Oradoc_db1",
    "cas.audit.jdbc.driver-class=oracle.jdbc.driver.OracleDriver",
    "cas.audit.jdbc.url=jdbc:oracle:thin:@localhost:1521:ORCLCDB",
    "cas.audit.jdbc.dialect=org.hibernate.dialect.Oracle12cDialect"
})
@EnabledIfPortOpen(port = 1521)
@Tag("Oracle")
public class CasSupportOracleJdbcAuditConfigurationTests extends CasSupportJdbcAuditConfigurationTests {
}
