package org.apereo.cas.web.support;

import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
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
import org.apereo.cas.config.CasMongoDbThrottlingConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.CasSupportMongoDbAuditConfiguration;
import org.apereo.cas.config.CasThrottlingConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;

import org.junit.experimental.categories.Category;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;

/**
 * This is  {@link MongoDbThrottledSubmissionHandlerInterceptorAdapterTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Category(MongoDbCategory.class)
@SpringBootTest(classes = {
    CasMongoDbThrottlingConfiguration.class,
    CasThrottlingConfiguration.class,
    CasCoreAuditConfiguration.class,
    CasCoreConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreLogoutConfiguration.class,
    RefreshAutoConfiguration.class,
    CasPersonDirectoryConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationPolicyConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationHandlersConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreServicesAuthenticationConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasSupportMongoDbAuditConfiguration.class,
    CasCoreWebConfiguration.class,
    CasRegisteredServicesTestConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class})
@TestPropertySource(properties = {
    "cas.authn.throttle.usernameParameter=username",
    "cas.audit.mongo.databaseName=throttle",
    "cas.audit.mongo.host=localhost",
    "cas.audit.mongo.port=27017",
    "cas.audit.mongo.collection=MongoDbCasThrottleRepository",
    "cas.audit.mongo.dropCollection=true",
    "cas.audit.mongo.userId=root",
    "cas.audit.mongo.password=secret",
    "cas.audit.mongo.authenticationDatabaseName=admin",
    "cas.audit.mongo.asynchronous=false"})
public class MongoDbThrottledSubmissionHandlerInterceptorAdapterTests extends
    BaseThrottledSubmissionHandlerInterceptorAdapterTests {
}
