package org.apereo.cas.audit;

import org.apereo.cas.category.PostgresCategory;
import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.RunningContinuousIntegrationCondition;

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
@ConditionalIgnore(condition = RunningContinuousIntegrationCondition.class, port = 5432)
@Category(PostgresCategory.class)
public class CasSupportPostgresJdbcAuditConfigurationTests extends CasSupportJdbcAuditConfigurationTests {
}
