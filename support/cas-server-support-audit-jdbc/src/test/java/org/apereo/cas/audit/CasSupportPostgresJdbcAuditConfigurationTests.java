package org.apereo.cas.audit;

import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSupportPostgresJdbcAuditConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@TestPropertySource(properties = {
    "cas.audit.jdbc.user=postgres",
    "cas.audit.jdbc.password=password",
    "cas.audit.jdbc.driverClass=org.postgresql.Driver",
    "cas.audit.jdbc.url=jdbc:postgresql://localhost:5432/audit",
    "cas.audit.jdbc.dialect=org.hibernate.dialect.PostgreSQL95Dialect"
})
@EnabledIfPortOpen(port = 5432)
@Tag("Postgres")
public class CasSupportPostgresJdbcAuditConfigurationTests extends CasSupportJdbcAuditConfigurationTests {
}
