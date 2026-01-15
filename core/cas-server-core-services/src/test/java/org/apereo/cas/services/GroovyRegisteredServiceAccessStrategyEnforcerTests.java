package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionStrategy;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyRegisteredServiceAccessStrategyEnforcerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Groovy")
@ExtendWith(CasTestExtension.class)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    GroovyRegisteredServiceAccessStrategyEnforcerTests.GroovyTestConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreScriptingAutoConfiguration.class,
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

    @TestConfiguration(value = "GroovyTestConfiguration", proxyBeanMethods = false)
    static class GroovyTestConfiguration {
        @Bean
        public AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan() {
            return new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy());
        }
    }
}
