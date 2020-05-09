package org.apereo.cas.services;

import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * Handles tests for {@link JpaServiceRegistry}
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@TestPropertySource(properties = {
    "cas.service-registry.jpa.user=postgres",
    "cas.service-registry.jpa.password=password",
    "cas.service-registry.jpa.driverClass=org.postgresql.Driver",
    "cas.service-registry.jpa.url=jdbc:postgresql://localhost:5432/services",
    "cas.service-registry.jpa.dialect=org.hibernate.dialect.PostgreSQL95Dialect"
})
@EnabledIfPortOpen(port = 5432)
@Tag("Postgres")
public class JpaServiceRegistryPostgresTests extends JpaServiceRegistryTests {
}
