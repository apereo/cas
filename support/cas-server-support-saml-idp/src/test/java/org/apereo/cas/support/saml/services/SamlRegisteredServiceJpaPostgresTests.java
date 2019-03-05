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
@TestPropertySource(locations = "classpath:samlpostgres.properties")
@EnabledIfPortOpen(port = 5432)
@EnabledIfContinuousIntegration
@Tag("Postgres")
public class SamlRegisteredServiceJpaPostgresTests extends SamlRegisteredServiceJpaTests {
}
