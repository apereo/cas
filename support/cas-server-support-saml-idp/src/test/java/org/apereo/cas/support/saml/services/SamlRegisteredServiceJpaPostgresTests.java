package org.apereo.cas.support.saml.services;

import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;
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
    "cas.serviceRegistry.jpa.user=postgres",
    "cas.serviceRegistry.jpa.password=password",
    "cas.serviceRegistry.jpa.driverClass=org.postgresql.Driver",
    "cas.serviceRegistry.jpa.url=jdbc:postgresql://localhost:5432/saml",
    "cas.serviceRegistry.jpa.dialect=org.hibernate.dialect.PostgreSQL95Dialect"
})
@EnabledIfPortOpen(port = 5432)
@EnabledIfContinuousIntegration
@Tag("Postgres")
public class SamlRegisteredServiceJpaPostgresTests extends SamlRegisteredServiceJpaTests {
}
