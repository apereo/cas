package org.apereo.cas.support.saml.services;

import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * The {@link SamlRegisteredServiceJpaPostgresTests} handles test cases for {@link SamlRegisteredService}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@TestPropertySource(properties = {
    "cas.service-registry.jpa.user=postgres",
    "cas.service-registry.jpa.password=password",
    "cas.service-registry.jpa.driverClass=org.postgresql.Driver",
    "cas.service-registry.jpa.url=jdbc:postgresql://localhost:5432/saml",
    "cas.service-registry.jpa.dialect=org.hibernate.dialect.PostgreSQL95Dialect"
})
@EnabledIfPortOpen(port = 5432)
@Tag("Postgres")
public class SamlRegisteredServiceJpaPostgresTests extends SamlRegisteredServiceJpaTests {
}
