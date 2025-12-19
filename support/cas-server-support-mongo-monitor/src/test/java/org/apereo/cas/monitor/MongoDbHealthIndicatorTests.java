package org.apereo.cas.monitor;

import module java.base;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasMongoDbMonitoringAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.mongo.CasMongoOperations;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.spring.beans.BeanContainer;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.common.web.ClientInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.health.contributor.Status;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MongoDbHealthIndicatorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("MongoDb")
@ExtendWith(CasTestExtension.class)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasMongoDbMonitoringAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasPersonDirectoryAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class
},
    properties = {
        "cas.monitor.mongo[0].user-id=root",
        "cas.monitor.mongo[0].password=secret",
        "cas.monitor.mongo[0].host=localhost",
        "cas.monitor.mongo[0].port=27017",
        "cas.monitor.mongo[0].authentication-database-name=admin",
        "cas.monitor.mongo[0].database-name=monitor"
    })
@EnabledIfListeningOnPort(port = 27017)
class MongoDbHealthIndicatorTests {
    @Autowired
    @Qualifier("mongoHealthIndicator")
    private HealthIndicator mongoHealthIndicator;

    @Autowired
    @Qualifier("mongoHealthIndicatorTemplate")
    private BeanContainer<CasMongoOperations> mongoHealthIndicatorTemplate;

    @BeforeEach
    void bootstrap() {
        val template = mongoHealthIndicatorTemplate.first();
        template.save(new AuditActionContext("casuser", "resource",
            "action", "appcode", LocalDateTime.now(Clock.systemUTC()),
            new ClientInfo("clientIp", "serverIp", UUID.randomUUID().toString(), "Paris")));
    }

    @Test
    void verifyMonitor() {
        val health = mongoHealthIndicator.health();
        assertEquals(Status.UP, health.getStatus());
        val details = (Map) health.getDetails().get(MongoDbHealthIndicator.class.getSimpleName() + "-monitor");
        assertTrue(details.containsKey("name"));

        details.values().forEach(value -> {
            if (value instanceof final Map map) {
                assertTrue(map.containsKey("size"));
                assertTrue(map.containsKey("capacity"));
                assertTrue(map.containsKey("evictions"));
                assertTrue(map.containsKey("percentFree"));
                assertTrue(map.containsKey("state"));
            }
        });
    }
}
