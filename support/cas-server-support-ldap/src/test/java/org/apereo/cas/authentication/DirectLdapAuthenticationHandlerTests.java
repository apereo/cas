package org.apereo.cas.authentication;

import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.RunningContinuousIntegrationCondition;

import org.springframework.test.context.TestPropertySource;

/**
 * Unit test for {@link LdapAuthenticationHandler}.
 *
 * @author Marvin S. Addison
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@TestPropertySource(locations = {"classpath:/ldapdirectauthn.properties"})
@ConditionalIgnore(condition = RunningContinuousIntegrationCondition.class)
public class DirectLdapAuthenticationHandlerTests extends BaseLdapAuthenticationHandlerTests {
}
