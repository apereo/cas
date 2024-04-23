package org.apereo.cas.services;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionStrategy;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Bean;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyRegisteredServiceAccessStrategyEnforcerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Groovy")
@SpringBootTest(classes = {
    GroovyRegisteredServiceAccessStrategyEnforcerTests.GroovyTestConfiguration.class,
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class
}, properties = "cas.access-strategy.groovy.location=classpath:ServiceAccessStrategy.groovy")
class GroovyRegisteredServiceAccessStrategyEnforcerTests {

    @Autowired
    @Qualifier("registeredServiceAccessStrategyEnforcer")
    private AuditableExecution registeredServiceAccessStrategyEnforcer;

    @Autowired
    @Qualifier("groovyRegisteredServiceAccessStrategyEnforcer")
    private RegisteredServiceAccessStrategyEnforcer groovyRegisteredServiceAccessStrategyEnforcer;
    
    @Test
    void verifyOperation() throws Throwable {
        assertNotNull(groovyRegisteredServiceAccessStrategyEnforcer);
        
        val context = AuditableContext.builder()
            .registeredService(RegisteredServiceTestUtils.getRegisteredService())
            .service(RegisteredServiceTestUtils.getService())
            .authentication(RegisteredServiceTestUtils.getAuthentication())
            .build();
        val results = registeredServiceAccessStrategyEnforcer.execute(context);
        assertTrue(results.isExecutionFailure());
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class GroovyTestConfiguration {
        @Bean
        public AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan() {
            return new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy());
        }
    }
}
