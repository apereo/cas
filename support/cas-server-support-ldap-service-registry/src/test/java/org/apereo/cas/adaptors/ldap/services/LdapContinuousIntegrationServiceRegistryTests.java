package org.apereo.cas.adaptors.ldap.services;

import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;

import org.springframework.test.context.TestPropertySource;

/**
 * Unit test for {@link LdapServiceRegistry} class.
 *
 * @author Misagh Moayyed
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@TestPropertySource(properties = {
    "cas.serviceRegistry.ldap.poolPassivator=NONE",
    "cas.serviceRegistry.ldap.bindDn=cn=Directory Manager",
    "cas.serviceRegistry.ldap.bindCredential=password",
    "cas.serviceRegistry.ldap.objectClass=account"
})
@EnabledIfContinuousIntegration
public class LdapContinuousIntegrationServiceRegistryTests extends BaseLdapServiceRegistryTests {
}
