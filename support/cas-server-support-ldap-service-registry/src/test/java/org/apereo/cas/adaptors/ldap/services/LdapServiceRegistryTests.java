package org.apereo.cas.adaptors.ldap.services;

import module java.base;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
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
    "cas.service-registry.ldap.pool-passivator=NONE",
    "cas.service-registry.ldap.bind-dn=cn=Directory Manager",
    "cas.service-registry.ldap.bind-credential=password",
    "cas.service-registry.ldap.object-class=account"
})
@EnabledIfListeningOnPort(port = 10389)
@Tag("LdapServices")
class LdapServiceRegistryTests extends BaseLdapServiceRegistryTests {
}
