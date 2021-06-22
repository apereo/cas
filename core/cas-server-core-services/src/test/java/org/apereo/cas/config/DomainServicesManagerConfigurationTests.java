package org.apereo.cas.config;

import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.services.ServicesManagerExecutionPlanConfigurer;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DomainServicesManagerConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("RegisteredService")
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreWebConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreNotificationsConfiguration.class
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
