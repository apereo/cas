package org.apereo.cas.config;

import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.services.ServicesManagerExecutionPlanConfigurer;

import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DomainServicesManagerConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("RegisteredService")
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreNotificationsConfiguration.class,
    CasServiceRegistryInitializationConfiguration.class
},
    properties = {
        "cas.service-registry.core.management-type=DOMAIN",
        "cas.service-registry.core.init-from-json=true"
    }
)
public class DomainServicesManagerConfigurationTests {
    @Autowired
    @Qualifier("domainServicesManagerExecutionPlanConfigurer")
    private ServicesManagerExecutionPlanConfigurer domainServicesManagerExecutionPlanConfigurer;

    @Test
    public void verifyOperation() {
        assertNotNull(domainServicesManagerExecutionPlanConfigurer);
    }
}
