package org.apereo.cas.monitor;

import org.apereo.cas.category.MongoDbCategory;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.monitor.config.MongoDbMonitoringConfiguration;
import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.ConditionalIgnoreRule;
import org.apereo.cas.util.junit.RunningContinuousIntegrationCondition;

import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.util.Date;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * This is {@link MongoDbHealthIndicatorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Category(MongoDbCategory.class)
@SpringBootTest(classes = {
    MongoDbMonitoringConfiguration.class,
    CasCoreTicketsConfiguration.class,
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
    CasCoreServicesConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCoreWebConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class
})
@TestPropertySource(properties = {
    "cas.monitor.mongo.userId=root",
    "cas.monitor.mongo.password=secret",
    "cas.monitor.mongo.host=localhost",
    "cas.monitor.mongo.port=27017",
    "cas.monitor.mongo.authenticationDatabaseName=admin",
    "cas.monitor.mongo.databaseName=monitor"
})
@ConditionalIgnore(condition = RunningContinuousIntegrationCondition.class, port = 27017)
public class MongoDbHealthIndicatorTests {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Rule
    public final ConditionalIgnoreRule conditionalIgnoreRule = new ConditionalIgnoreRule();

    @Autowired
    @Qualifier("mongoHealthIndicator")
    private HealthIndicator mongoHealthIndicator;

    @Autowired
    @Qualifier("mongoHealthIndicatorTemplate")
    private MongoTemplate template;

    @BeforeEach
    public void bootstrap(){
        template.save(new AuditActionContext("casuser", "resource",
            "action", "appcode", new Date(), "clientIp",
            "serverIp"), "monitor");
    }

    @Test
    public void verifyMonitor() {
        val health = mongoHealthIndicator.health();
        assertEquals(Status.UP, health.getStatus());
        val details = health.getDetails();
        details.values().stream()
            .map(Map.class::cast)
            .forEach(map -> {
                assertTrue(map.containsKey("size"));
                assertTrue(map.containsKey("capacity"));
                assertTrue(map.containsKey("evictions"));
                assertTrue(map.containsKey("percentFree"));
            });
        assertNotNull(mongoHealthIndicator.toString());
    }
}
