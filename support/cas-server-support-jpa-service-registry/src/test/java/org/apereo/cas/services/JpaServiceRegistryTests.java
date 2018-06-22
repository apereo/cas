package org.apereo.cas.services;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.JpaServiceRegistryConfiguration;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.junit.ConditionalSpringRunner;
import org.joda.time.DateTimeUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;


/**
 * Handles tests for {@link JpaServiceRegistry}
 *
 * @author battags
 * @since 3.1.0
 */
@RunWith(ConditionalSpringRunner.class)
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    AopAutoConfiguration.class,
    CasCoreUtilConfiguration.class,
    JpaServiceRegistryConfiguration.class,
    JpaServiceRegistryTests.TimeAwareServicesManagerConfiguration.class,
    CasCoreServicesConfiguration.class})
@DirtiesContext
@Slf4j
public class JpaServiceRegistryTests extends AbstractServiceRegistryTests {

    @Autowired
    @Qualifier("jpaServiceRegistry")
    private ServiceRegistry serviceRegistry;

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
        final RegisteredService r2 = this.serviceRegistry.save(r);
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
        final SamlRegisteredService r2 = (SamlRegisteredService) this.serviceRegistry.save(r);
        assertEquals(r, r2);
    }


    @Override
    public ServiceRegistry getNewServiceRegistry() {
        return this.serviceRegistry;
    }

    @TestConfiguration("timeAwareServicesManagerConfiguration")
    public static class TimeAwareServicesManagerConfiguration {

        @Autowired
        @Qualifier("serviceRegistry")
        private ServiceRegistry serviceRegistry;

        @Bean
        public ServicesManager servicesManager() {
            return new TimeAwareServicesManager(serviceRegistry);
        }

        public static class TimeAwareServicesManager extends DefaultServicesManager {
            public TimeAwareServicesManager(final ServiceRegistry serviceRegistry) {
                super(serviceRegistry, null);
            }

            @Override
            protected LocalDateTime getCurrentSystemTime() {
                return org.apereo.cas.util.DateTimeUtils.localDateTimeOf(DateTimeUtils.currentTimeMillis());
            }
        }
    }
}
