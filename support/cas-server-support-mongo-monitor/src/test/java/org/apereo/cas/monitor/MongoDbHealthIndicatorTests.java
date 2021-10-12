package org.apereo.cas.monitor;

import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.monitor.config.MongoDbMonitoringConfiguration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;
import org.apereo.cas.util.spring.BeanContainer;

import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MongoDbHealthIndicatorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("MongoDb")
@SpringBootTest(classes = {
    MongoDbMonitoringConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasPersonDirectoryConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreServicesAuthenticationConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationPolicyConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationHandlersConfiguration.class,
    CasCoreHttpConfiguration.class,
    RefreshAutoConfiguration.class,
    CasCoreConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    CasCoreNotificationsConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreWebConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class
},
    properties = {
        "cas.monitor.mongo[0].user-id=root",
        "cas.monitor.mongo[0].password=secret",
        "cas.monitor.mongo[0].host=localhost",
        "cas.monitor.mongo[0].port=27017",
        "cas.monitor.mongo[0].authentication-database-name=admin",
        "cas.monitor.mongo[0].database-name=monitor"
    })
@EnabledIfPortOpen(port = 27017)
@SuppressWarnings("JavaUtilDate")
public class MongoDbHealthIndicatorTests {
    @Autowired
    @Qualifier("mongoHealthIndicator")
    private HealthIndicator mongoHealthIndicator;

    @Autowired
    @Qualifier("mongoHealthIndicatorTemplate")
    private BeanContainer<MongoTemplate> mongoHealthIndicatorTemplate;

    @BeforeEach
    public void bootstrap() {
        val template = mongoHealthIndicatorTemplate.first();
        template.save(new AuditActionContext("casuser", "resource",
            "action", "appcode", new Date(), "clientIp",
            "serverIp"), "monitor");
    }

    @Test
    public void verifyMonitor() {
        val health = mongoHealthIndicator.health();
        assertEquals(Status.UP, health.getStatus());
        val details = (Map) health.getDetails().get(MongoDbHealthIndicator.class.getSimpleName() + "-monitor");
        assertTrue(details.containsKey("name"));

        details.values().forEach(value -> {
            if (value instanceof Map) {
                val map = (Map<String, ?>) value;
                assertTrue(map.containsKey("size"));
                assertTrue(map.containsKey("capacity"));
                assertTrue(map.containsKey("evictions"));
                assertTrue(map.containsKey("percentFree"));
                assertTrue(map.containsKey("state"));
            }
        });
    }
}
