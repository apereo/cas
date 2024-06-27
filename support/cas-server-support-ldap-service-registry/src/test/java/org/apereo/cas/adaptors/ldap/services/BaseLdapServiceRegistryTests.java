package org.apereo.cas.adaptors.ldap.services;

import org.apereo.cas.config.CasLdapServiceRegistryAutoConfiguration;
import org.apereo.cas.services.AbstractServiceRegistryTests;
import org.apereo.cas.services.CasRegisteredService;
import org.apereo.cas.services.RegisteredServiceDefinition;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.test.CasTestExtension;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableScheduling;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BaseLdapServiceRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@EnableScheduling
@SpringBootTest(classes = {
    CasLdapServiceRegistryAutoConfiguration.class,
    AbstractServiceRegistryTests.SharedTestConfiguration.class
}, properties = {
    "cas.service-registry.ldap.ldap-url=ldap://localhost:10389",
    "cas.service-registry.ldap.base-dn=dc=example,dc=org"
})
@Getter
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(CasTestExtension.class)
public abstract class BaseLdapServiceRegistryTests extends AbstractServiceRegistryTests {

    @Autowired
    @Qualifier("ldapServiceRegistry")
    private ServiceRegistry newServiceRegistry;

    @Autowired
    @Qualifier("ldapServiceRegistryMapper")
    private LdapRegisteredServiceMapper ldapServiceRegistryMapper;


    @Test
    void verifySavingServiceChangesDn() {
        getRegisteredServiceTypes().forEach(registeredServiceClass -> {
            getServiceRegistry().save(buildRegisteredServiceInstance(8080, registeredServiceClass));
            val services = getServiceRegistry().load();
            assertFalse(services.isEmpty());
            val rs = getServiceRegistry().findServiceById(services.iterator().next().getId());
            val originalId = rs.getId();
            assertNotNull(rs);
            rs.setId(666);
            assertNotNull(getServiceRegistry().save(rs));
            assertNotEquals(rs.getId(), originalId);

            assertNotNull(ldapServiceRegistryMapper.getIdAttribute());
            assertNotNull(ldapServiceRegistryMapper.getObjectClass());
        });
    }

    @Test
    void verifyServiceInserted() {
        val registeredService = buildRegisteredServiceInstance(998877, CasRegisteredService.class);
        registeredService.setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE);
        getServiceRegistry().save(registeredService);
        val services = getServiceRegistry().load();
        assertFalse(services.isEmpty());
        val rs = getServiceRegistry().findServiceById(registeredService.getId());
        assertNotNull(rs);
    }
}
