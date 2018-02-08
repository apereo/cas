package org.apereo.cas.adaptors.ldap.services;

import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.junit.Before;
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

    @Before
    public void setup() {
        LdapIntegrationTestsOperations.checkContinuousIntegrationBuild(true);
    }
}
