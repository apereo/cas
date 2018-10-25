package org.apereo.cas.adaptors.ldap.services;

import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.junit.DisabledIfContinuousIntegration;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

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

    public LdapServiceRegistryTests(final Class<? extends RegisteredService> registeredServiceClass) {
        super(registeredServiceClass);
    }

    public static void bootstrap() throws Exception {
        LdapIntegrationTestsOperations.initDirectoryServer(1390);
    }

    @Test
    public void verifySavingServiceChangesDn() {
        getServiceRegistry().save(buildRegisteredServiceInstance(8080));
        val services = getServiceRegistry().load();
        assertFalse(services.isEmpty());
        val rs = getServiceRegistry().findServiceById(services.stream().findFirst().orElse(null).getId());
        val originalId = rs.getId();
        assertNotNull(rs);
        rs.setId(666);
        assertNotNull(getServiceRegistry().save(rs));
        assertNotEquals(rs.getId(), originalId);
    }
}
