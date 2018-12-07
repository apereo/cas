package org.apereo.cas.support.saml.services;

import org.apereo.cas.util.test.junit.EnabledIfContinuousIntegration;
import org.apereo.cas.util.test.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * The {@link SamlRegisteredServiceJpaPostgresTests} handles test cases for {@link SamlRegisteredService}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@TestPropertySource(locations = "classpath:samlpostgres.properties")
@EnabledIfContinuousIntegration
@EnabledIfPortOpen(port = 5432)
@Tag("postgres")
public class SamlRegisteredServiceJpaPostgresTests extends SamlRegisteredServiceJpaTests {
}
