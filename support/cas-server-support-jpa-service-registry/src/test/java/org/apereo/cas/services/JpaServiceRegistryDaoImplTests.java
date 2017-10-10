package org.apereo.cas.services;

import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.JpaServiceRegistryConfiguration;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.joda.time.DateTimeUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;


/**
 * Handles tests for {@link JpaServiceRegistryDaoImpl}
 *
 * @author battags
 * @since 3.1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RefreshAutoConfiguration.class,
        CasCoreUtilConfiguration.class,
        JpaServiceRegistryConfiguration.class,
        JpaServiceRegistryDaoImplTests.TimeAwareServicesManagerConfiguration.class,
        CasCoreServicesConfiguration.class})
@DirtiesContext
public class JpaServiceRegistryDaoImplTests {

    @Autowired
    @Qualifier("serviceRegistryDao")
    private ServiceRegistryDao serviceRegistryDao;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Before
    public void setUp() {
        final List<RegisteredService> services = this.serviceRegistryDao.load();
        services.forEach(service -> this.serviceRegistryDao.delete(service));
    }

    @Test
    public void verifySaveMethodWithNonExistentServiceAndNoAttributes() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setName("verifySaveMethodWithNonExistentServiceAndNoAttributes");
        r.setServiceId("testId");
        r.setTheme("theme");
        r.setDescription("description");
        r.setPublicKey(new RegisteredServicePublicKeyImpl("classpath:/test.pub", "RSA"));

        final RegisteredService r2 = this.serviceRegistryDao.save(r);
        final RegisteredService r3 = this.serviceRegistryDao.findServiceById(r2.getId());

        assertEquals(r, r2);
        assertEquals(r2, r3);
    }

    @Test
    public void verifySaveAttributeReleasePolicy() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setName("verifySaveAttributeReleasePolicy");
        r.setServiceId("testId");
        r.setTheme("theme");
        r.setDescription("description");
        r.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());

        final RegisteredService r2 = this.serviceRegistryDao.save(r);
        final RegisteredService r3 = this.serviceRegistryDao.findServiceById(r2.getId());

        assertEquals(r, r2);
        assertEquals(r2, r3);
        assertNotNull(r3.getAttributeReleasePolicy());
        assertEquals(r2.getAttributeReleasePolicy(), r3.getAttributeReleasePolicy());
    }

    @Test
    public void verifySaveMethodWithExistingServiceNoAttribute() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setName("verifySaveMethodWithExistingServiceNoAttribute");
        r.setServiceId("testId");
        r.setTheme("theme");
        r.setDescription("description");

        this.serviceRegistryDao.save(r);

        final List<RegisteredService> services = this.serviceRegistryDao.load();
        final RegisteredService r2 = services.get(0);

        r.setId(r2.getId());
        this.serviceRegistryDao.save(r);

        final RegisteredService r3 = this.serviceRegistryDao.findServiceById(r.getId());

        assertEquals(r, r2);
        assertEquals(r.getTheme(), r3.getTheme());
    }

    @Test
    public void verifyRegisteredServiceProperties() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setName("test");
        r.setServiceId("testId");
        r.setTheme("theme");
        r.setDescription("description");

        final Map<String, RegisteredServiceProperty> propertyMap = new HashMap<>();

        final DefaultRegisteredServiceProperty property = new DefaultRegisteredServiceProperty();
        final Set<String> values = new HashSet<>();
        values.add("value1");
        values.add("value2");
        property.setValues(values);
        propertyMap.put("field1", property);

        final DefaultRegisteredServiceProperty property2 = new DefaultRegisteredServiceProperty();

        final Set<String> values2 = new HashSet<>();
        values2.add("value1");
        values2.add("value2");
        property2.setValues(values2);
        propertyMap.put("field2", property2);

        r.setProperties(propertyMap);

        this.serviceRegistryDao.save(r);

        final RegisteredService r2 = this.serviceRegistryDao.load().get(0);
        assertEquals(r2.getProperties().size(), 2);
    }

    @Test
    public void verifyOAuthServices() {
        final OAuthRegisteredService r = new OAuthRegisteredService();
        r.setName("verifyOAuthServices");
        r.setServiceId("testId");
        r.setTheme("theme");
        r.setDescription("description");
        r.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        r.setClientId("testoauthservice");
        r.setClientSecret("anothertest");
        r.setBypassApprovalPrompt(true);
        final RegisteredService r2 = this.serviceRegistryDao.save(r);
        assertEquals(r, r2);
    }

    @Test
    public void verifySamlService() {
        final SamlRegisteredService r = new SamlRegisteredService();
        r.setName("verifySamlService");
        r.setServiceId("Testing");
        r.setDescription("description");
        r.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        final Map fmt = new HashMap();
        fmt.put("key", "value");
        r.setAttributeNameFormats(fmt);
        r.setMetadataCriteriaDirection("INCLUDE");
        r.setMetadataCriteriaRemoveEmptyEntitiesDescriptors(true);
        r.setMetadataSignatureLocation("location");
        r.setRequiredAuthenticationContextClass("Testing");
        final SamlRegisteredService r2 = (SamlRegisteredService) this.serviceRegistryDao.save(r);
        assertEquals(r, r2);
    }

    @Test
    public void verifyExpiredServiceDeleted() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setServiceId("testExpired");
        r.setName("expired");
        r.setExpirationPolicy(new DefaultRegisteredServiceExpirationPolicy(true, LocalDateTime.now().minusSeconds(1)));
        final RegisteredService r2 = this.servicesManager.save(r);
        DateTimeUtils.setCurrentMillisFixed(System.currentTimeMillis() + 2000);
        this.servicesManager.load();
        final RegisteredService svc = this.servicesManager.findServiceBy(r2.getServiceId());
        assertNull(svc);
    }

    @Test
    public void verifyExpiredServiceDisabled() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setServiceId("testExpired1");
        r.setName("expired1");
        final LocalDateTime expirationDate = LocalDateTime.now().plusSeconds(1);
        r.setExpirationPolicy(new DefaultRegisteredServiceExpirationPolicy(false, expirationDate));
        final RegisteredService r2 = this.servicesManager.save(r);
        RegisteredService svc = this.servicesManager.findServiceBy(r2.getServiceId());
        assertNotNull(svc);
        DateTimeUtils.setCurrentMillisFixed(System.currentTimeMillis() + 2000);
        svc = this.servicesManager.findServiceBy(r2.getServiceId());
        assertNotNull(svc);
        assertFalse(svc.getAccessStrategy().isServiceAccessAllowed());
    }
    
    @TestConfiguration("timeAwareServicesManagerConfiguration")
    public static class TimeAwareServicesManagerConfiguration {

        @Autowired
        @Qualifier("serviceRegistryDao")
        private ServiceRegistryDao serviceRegistryDao;
        
        @Bean
        public ServicesManager servicesManager() {
            return new TimeAwareServicesManager(serviceRegistryDao);
        }
        
        public class TimeAwareServicesManager extends DefaultServicesManager {
            public TimeAwareServicesManager(final ServiceRegistryDao serviceRegistryDao) {
                super(serviceRegistryDao, null);
            }

            @Override
            protected LocalDateTime getCurrentSystemTime() {
                return org.apereo.cas.util.DateTimeUtils.localDateTimeOf(DateTimeUtils.currentTimeMillis());
            }
        }
    }
}
