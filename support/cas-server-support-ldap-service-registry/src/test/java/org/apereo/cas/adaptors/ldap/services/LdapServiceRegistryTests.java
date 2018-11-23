package org.apereo.cas.adaptors.ldap.services;

import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.util.test.junit.DisabledIfContinuousIntegration;

import org.springframework.test.context.TestPropertySource;

/**
 * Unit test for {@link LdapServiceRegistry} class.
 *
 * @author Misagh Moayyed
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@TestPropertySource(properties = {
    "cas.serviceRegistry.ldap.ldapUrl=ldap://localhost:1390",
    "cas.serviceRegistry.ldap.useSsl=false",
    "cas.serviceRegistry.ldap.baseDn=dc=example,dc=org"
})
@DisabledIfContinuousIntegration
public class LdapServiceRegistryTests extends BaseLdapServiceRegistryTests {

    public static void bootstrap() throws Exception {
        LdapIntegrationTestsOperations.initDirectoryServer(1390);
    }
}
