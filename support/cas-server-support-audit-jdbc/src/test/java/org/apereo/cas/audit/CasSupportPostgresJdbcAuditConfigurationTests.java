package org.apereo.cas.audit;

import org.apereo.cas.category.PostgresCategory;
import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.experimental.categories.Category;
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
    "cas.audit.jdbc.url=jdbc:postgresql://localhost:5432/postgres",
    "cas.audit.jdbc.dialect=org.hibernate.dialect.PostgreSQL95Dialect"
})
@EnabledIfContinuousIntegration
@EnabledIfPortOpen(port = 5432)
@Category(PostgresCategory.class)
public class CasSupportPostgresJdbcAuditConfigurationTests extends CasSupportJdbcAuditConfigurationTests {
}
