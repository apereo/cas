package org.apereo.cas.pm.jdbc;

import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link PostgresJdbcPasswordHistoryServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@TestPropertySource(properties = {
    "cas.jdbc.showSql=true",
    "cas.authn.pm.jdbc.user=postgres",
    "cas.authn.pm.jdbc.password=password",
    "cas.authn.pm.jdbc.driverClass=org.postgresql.Driver",
    "cas.authn.pm.jdbc.url=jdbc:postgresql://localhost:5432/pm",
    "cas.authn.pm.jdbc.dialect=org.hibernate.dialect.PostgreSQL95Dialect",
    "cas.authn.pm.history.enabled=true"
})
@EnabledIfPortOpen(port = 5432)
@Tag("Postgres")
public class PostgresJdbcPasswordHistoryServiceTests extends JdbcPasswordHistoryServiceTests {

    @BeforeEach
    public void initialize() {
        passwordHistoryService.removeAll();
    }
}
