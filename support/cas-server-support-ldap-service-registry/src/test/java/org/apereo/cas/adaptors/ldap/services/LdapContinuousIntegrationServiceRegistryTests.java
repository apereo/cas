package org.apereo.cas.adaptors.ldap.services;

import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.ConditionalSpringRunner;
import org.apereo.cas.util.junit.RunningContinuousIntegrationCondition;
import org.junit.runner.RunWith;
import org.springframework.test.context.TestPropertySource;

/**
 * Unit test for {@link LdapServiceRegistry} class.
 *
 * @author Misagh Moayyed
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@TestPropertySource(locations = "classpath:/ldapsvc-ci.properties")
@RunWith(ConditionalSpringRunner.class)
@ConditionalIgnore(condition = RunningContinuousIntegrationCondition.class)
public class LdapContinuousIntegrationServiceRegistryTests extends BaseLdapServiceRegistryTests {
}
