package org.apereo.cas.adaptors.ldap.services;

import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestMethodOrder;
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
@EnabledIfPortOpen(port = 10389)
@Tag("Ldap")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LdapServiceRegistryTests extends BaseLdapServiceRegistryTests {
}
