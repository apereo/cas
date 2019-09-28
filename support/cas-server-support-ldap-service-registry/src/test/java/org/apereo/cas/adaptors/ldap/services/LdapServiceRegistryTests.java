package org.apereo.cas.adaptors.ldap.services;

import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.util.junit.DisabledIfContinuousIntegration;

import org.junit.jupiter.api.BeforeAll;

/**
 * Unit test for {@link LdapServiceRegistry} class.
 *
 * @author Misagh Moayyed
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@DisabledIfContinuousIntegration
public class LdapServiceRegistryTests extends BaseLdapServiceRegistryTests {

    @BeforeAll
    public static void bootstrap() throws Exception {
        LdapIntegrationTestsOperations.initDirectoryServer(10389);
    }
}
