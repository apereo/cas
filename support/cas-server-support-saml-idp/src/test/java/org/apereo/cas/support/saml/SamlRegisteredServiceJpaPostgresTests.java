package org.apereo.cas.support.saml;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.category.PostgresCategory;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.RunningContinuousIntegrationCondition;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.TestPropertySource;

/**
 * The {@link SamlRegisteredServiceJpaPostgresTests} handles test cases for {@link SamlRegisteredService}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@TestPropertySource(locations = "classpath:samlpostgres.properties")
@ConditionalIgnore(condition = RunningContinuousIntegrationCondition.class, port = 5432)
@Category(PostgresCategory.class)
public class SamlRegisteredServiceJpaPostgresTests extends SamlRegisteredServiceJpaTests {

}
