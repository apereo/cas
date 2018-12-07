package org.apereo.cas.adaptors.ldap.services;

import org.apereo.cas.adaptors.ldap.services.config.LdapServiceRegistryConfiguration;
import org.apereo.cas.services.AbstractServiceRegistryTests;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServiceRegistry;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.annotation.DirtiesContext;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BaseLdapServiceRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@EnableScheduling
@DirtiesContext
@Tag("ldap")
@SpringBootTest(classes = {LdapServiceRegistryConfiguration.class, RefreshAutoConfiguration.class})
public abstract class BaseLdapServiceRegistryTests extends AbstractServiceRegistryTests {

    @Autowired
    @Qualifier("ldapServiceRegistry")
    private ServiceRegistry dao;

    @Override
    public ServiceRegistry getNewServiceRegistry() {
        return this.dao;
    }

    public static Stream<Class<? extends RegisteredService>> getParameters() {
        return AbstractServiceRegistryTests.getParameters();
    }

    @ParameterizedTest
    @MethodSource("getParameters")
    public void verifySavingServiceChangesDn(final Class<? extends RegisteredService> registeredServiceClass) {
        getServiceRegistry().save(buildRegisteredServiceInstance(8080, registeredServiceClass));
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
