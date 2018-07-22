package org.apereo.cas.support.saml;

import org.apereo.cas.category.MsSqlServerCategory;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.RunningContinuousIntegrationCondition;

import org.junit.experimental.categories.Category;
import org.springframework.test.context.TestPropertySource;

/**
 * The {@link SamlRegisteredServiceJpaMicrosoftSqlServerTests} handles test cases for {@link SamlRegisteredService}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@TestPropertySource(locations = "classpath:samlsqlserver.properties")
@ConditionalIgnore(condition = RunningContinuousIntegrationCondition.class, port = 1433)
@Category(MsSqlServerCategory.class)
public class SamlRegisteredServiceJpaMicrosoftSqlServerTests extends SamlRegisteredServiceJpaTests {

}
