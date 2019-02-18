package org.apereo.cas.support.saml.services;

import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * The {@link SamlRegisteredServiceJpaMicrosoftSqlServerTests} handles test cases for {@link SamlRegisteredService}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@TestPropertySource(locations = "classpath:samlsqlserver.properties")
@EnabledIfPortOpen(port = 1433)
@EnabledIfContinuousIntegration
@Tag("MsSqlServer")
public class SamlRegisteredServiceJpaMicrosoftSqlServerTests extends SamlRegisteredServiceJpaTests {

}
