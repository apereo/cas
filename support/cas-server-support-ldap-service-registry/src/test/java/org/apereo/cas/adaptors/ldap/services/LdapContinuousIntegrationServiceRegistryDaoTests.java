package org.apereo.cas.adaptors.ldap.services;

import org.apereo.cas.util.CoreTestUtils;
import org.apereo.cas.util.junit.ConditionalIgnoreRule;
import org.junit.Before;
import org.junit.Rule;
import org.springframework.test.context.TestPropertySource;

/**
 * Unit test for {@link LdapServiceRegistryDao} class.
 *
 * @author Misagh Moayyed
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@TestPropertySource(locations = "classpath:/ldapsvc-ci.properties")
public class LdapContinuousIntegrationServiceRegistryDaoTests extends BaseLdapServiceRegistryDaoTests {
    @Rule
    public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    @Before
    public void setup() {
        CoreTestUtils.checkContinuousIntegrationBuild(true);
    }
}
