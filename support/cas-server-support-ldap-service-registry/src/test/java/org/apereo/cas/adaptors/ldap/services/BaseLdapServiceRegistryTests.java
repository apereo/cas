package org.apereo.cas.adaptors.ldap.services;

import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.config.LdapServiceRegistryConfiguration;
import org.apereo.cas.services.AbstractServiceRegistryTests;
import org.apereo.cas.services.BaseWebBasedRegisteredService;
import org.apereo.cas.services.CasRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServiceRegistry;

import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BaseLdapServiceRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@EnableScheduling
@SpringBootTest(classes = {
    LdapServiceRegistryConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreWebConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreNotificationsConfiguration.class,
    RefreshAutoConfiguration.class
}, properties = {
    "cas.service-registry.ldap.ldap-url=ldap://localhost:10389",
    "cas.service-registry.ldap.base-dn=dc=example,dc=org"
})
@Getter
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class BaseLdapServiceRegistryTests extends AbstractServiceRegistryTests {

    @Autowired
    @Qualifier("ldapServiceRegistry")
    private ServiceRegistry newServiceRegistry;

    @Autowired
    @Qualifier("ldapServiceRegistryMapper")
    private LdapRegisteredServiceMapper ldapServiceRegistryMapper;

    public static Stream<Class<? extends RegisteredService>> getParameters() {
        return AbstractServiceRegistryTests.getParameters();
    }

    @ParameterizedTest
    @MethodSource("getParameters")
    void verifySavingServiceChangesDn(final Class<? extends BaseWebBasedRegisteredService> registeredServiceClass) {
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
    }

    @Test
    void verifyServiceInserted() throws Throwable {
        val registeredService = buildRegisteredServiceInstance(998877, CasRegisteredService.class);
        registeredService.setId(RegisteredService.INITIAL_IDENTIFIER_VALUE);
        getServiceRegistry().save(registeredService);
        val services = getServiceRegistry().load();
        assertFalse(services.isEmpty());
        val rs = getServiceRegistry().findServiceById(registeredService.getId());
        assertNotNull(rs);
    }
}
