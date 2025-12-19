package org.apereo.cas.support.saml.services;

import module java.base;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
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
    "cas.service-registry.jpa.driver-class=org.postgresql.Driver",
    "cas.service-registry.jpa.url=jdbc:postgresql://localhost:5432/saml",
    "cas.service-registry.jpa.dialect=org.hibernate.dialect.PostgreSQLDialect"
})
@EnabledIfListeningOnPort(port = 5432)
@Tag("Postgres")
class SamlRegisteredServiceJpaPostgresTests extends SamlRegisteredServiceJpaTests {
}
