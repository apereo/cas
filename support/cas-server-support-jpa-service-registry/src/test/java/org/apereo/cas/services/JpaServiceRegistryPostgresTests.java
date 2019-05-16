package org.apereo.cas.services;

import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;
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
    "cas.serviceRegistry.jpa.user=postgres",
    "cas.serviceRegistry.jpa.password=password",
    "cas.serviceRegistry.jpa.driverClass=org.postgresql.Driver",
    "cas.serviceRegistry.jpa.url=jdbc:postgresql://localhost:5432/services",
    "cas.serviceRegistry.jpa.dialect=org.hibernate.dialect.PostgreSQL95Dialect"
})
@EnabledIfContinuousIntegration
@EnabledIfPortOpen(port = 5432)
@Tag("Postgres")
public class JpaServiceRegistryPostgresTests extends JpaServiceRegistryTests {
}
