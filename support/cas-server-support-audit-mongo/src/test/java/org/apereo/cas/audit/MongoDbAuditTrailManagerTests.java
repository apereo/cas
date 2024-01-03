package org.apereo.cas.audit;

import org.apereo.cas.audit.spi.BaseAuditConfigurationTests;
import org.apereo.cas.config.CasCoreAuditAutoConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasSupportMongoDbAuditConfiguration;
import org.apereo.cas.config.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.Getter;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

/**
 * This is {@link MongoDbAuditTrailManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@SpringBootTest(classes = {
    CasCoreAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreAuditAutoConfiguration.class,
    CasSupportMongoDbAuditConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class
},
    properties = {
        "cas.audit.mongo.host=localhost",
        "cas.audit.mongo.port=27017",
        "cas.audit.mongo.drop-collection=true",
        "cas.audit.mongo.asynchronous=false",
        "cas.audit.mongo.user-id=root",
        "cas.audit.mongo.password=secret",
        "cas.audit.mongo.database-name=audit",
        "cas.audit.mongo.authentication-database-name=admin"
    })
@Tag("MongoDb")
@Getter
@EnabledIfListeningOnPort(port = 27017)
class MongoDbAuditTrailManagerTests extends BaseAuditConfigurationTests {

    @Autowired
    @Qualifier("mongoDbAuditTrailManager")
    private AuditTrailManager auditTrailManager;
}
