package org.apereo.cas.adaptors.ldap.services;

import org.apereo.cas.category.LdapCategory;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;

import org.junit.experimental.categories.Category;
import org.springframework.test.context.TestPropertySource;

/**
 * Unit test for {@link LdapServiceRegistry} class.
 *
 * @author Misagh Moayyed
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@TestPropertySource(properties = {
    "cas.serviceRegistry.ldap.ldapUrl=ldap://localhost:10389",
    "cas.serviceRegistry.ldap.useSsl=false",
    "cas.serviceRegistry.ldap.baseDn=dc=example,dc=org",
    "cas.serviceRegistry.ldap.bindDn=cn=Directory Manager",
    "cas.serviceRegistry.ldap.bindCredential=password",
    "cas.serviceRegistry.ldap.objectClass=account"
})
@Category(LdapCategory.class)
@EnabledIfContinuousIntegration
public class LdapContinuousIntegrationServiceRegistryTests extends BaseLdapServiceRegistryTests {
    public LdapContinuousIntegrationServiceRegistryTests(final Class<? extends RegisteredService> registeredServiceClass) {
        super(registeredServiceClass);
    }
}
