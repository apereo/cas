package org.apereo.cas.services;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyRegisteredServiceAccessStrategyEnforcerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Groovy")
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreNotificationsConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreWebConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class
}, properties = "cas.access-strategy.groovy.location=classpath:ServiceAccessStrategy.groovy")
public class GroovyRegisteredServiceAccessStrategyEnforcerTests {

    @Autowired
    @Qualifier("registeredServiceAccessStrategyEnforcer")
    private AuditableExecution registeredServiceAccessStrategyEnforcer;

    @Autowired
    @Qualifier("groovyRegisteredServiceAccessStrategyEnforcer")
    private RegisteredServiceAccessStrategyEnforcer groovyRegisteredServiceAccessStrategyEnforcer;
    
    @Test
    public void verifyOperation() throws Exception {
        assertNotNull(groovyRegisteredServiceAccessStrategyEnforcer);
        
        val context = AuditableContext.builder()
            .registeredService(RegisteredServiceTestUtils.getRegisteredService())
            .service(RegisteredServiceTestUtils.getService())
            .authentication(RegisteredServiceTestUtils.getAuthentication())
            .build();
        val results = registeredServiceAccessStrategyEnforcer.execute(context);
        assertTrue(results.isExecutionFailure());
    }
}
