package org.apereo.cas.support.saml;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.RunningContinuousIntegrationCondition;
import org.springframework.test.context.TestPropertySource;

/**
 * The {@link SamlRegisteredServiceJpaMicrosoftSqlServerTests} handles test cases for {@link SamlRegisteredService}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@TestPropertySource(locations = "classpath:samlsqlserver.properties")
@ConditionalIgnore(condition = RunningContinuousIntegrationCondition.class, port = 1433)
public class SamlRegisteredServiceJpaMicrosoftSqlServerTests extends SamlRegisteredServiceJpaTests {
}
