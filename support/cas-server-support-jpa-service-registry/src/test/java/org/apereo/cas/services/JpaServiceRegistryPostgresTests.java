package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
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
    "cas.service-registry.jpa.driver-class=org.postgresql.Driver",
    "cas.service-registry.jpa.url=jdbc:postgresql://localhost:5432/services",
    "cas.service-registry.jpa.dialect=org.hibernate.dialect.PostgreSQLDialect"
})
@EnabledIfListeningOnPort(port = 5432)
@Tag("Postgres")
class JpaServiceRegistryPostgresTests extends JpaServiceRegistryTests {

}
