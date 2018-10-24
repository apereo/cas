package org.apereo.cas.support.saml.services;

import org.apereo.cas.category.MsSqlServerCategory;
import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.experimental.categories.Category;
import org.springframework.test.context.TestPropertySource;

/**
 * The {@link SamlRegisteredServiceJpaMicrosoftSqlServerTests} handles test cases for {@link SamlRegisteredService}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@TestPropertySource(locations = "classpath:samlsqlserver.properties")
@EnabledIfContinuousIntegration
@EnabledIfPortOpen(port = 1433)
@Category(MsSqlServerCategory.class)
public class SamlRegisteredServiceJpaMicrosoftSqlServerTests extends SamlRegisteredServiceJpaTests {

}
