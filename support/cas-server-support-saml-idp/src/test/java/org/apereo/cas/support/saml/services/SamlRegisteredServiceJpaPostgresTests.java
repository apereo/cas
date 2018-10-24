package org.apereo.cas.support.saml.services;

import org.apereo.cas.category.PostgresCategory;
import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.experimental.categories.Category;
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
@Category(PostgresCategory.class)
public class SamlRegisteredServiceJpaPostgresTests extends SamlRegisteredServiceJpaTests {
}
