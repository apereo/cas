package org.apereo.cas.adaptors.ldap.services;

import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * Unit test for {@link LdapServiceRegistry} class.
 *
 * @author Misagh Moayyed
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@TestPropertySource(properties = {
    "cas.service-registry.ldap.poolPassivator=NONE",
    "cas.service-registry.ldap.bindDn=cn=Directory Manager",
    "cas.service-registry.ldap.bindCredential=password",
    "cas.service-registry.ldap.objectClass=account"
})
@EnabledIfPortOpen(port = 10389)
@Tag("Ldap")
public class LdapServiceRegistryTests extends BaseLdapServiceRegistryTests {
}
