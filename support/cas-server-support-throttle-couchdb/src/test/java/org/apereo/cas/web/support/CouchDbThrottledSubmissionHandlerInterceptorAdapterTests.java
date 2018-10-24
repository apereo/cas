package org.apereo.cas.web.support;

import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.category.CouchDbCategory;
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
import org.apereo.cas.config.CasCouchDbCoreConfiguration;
import org.apereo.cas.config.CasCouchDbThrottlingConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.CasSupportCouchDbAuditConfiguration;
import org.apereo.cas.config.CasThrottlingConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.couchdb.audit.AuditActionContextCouchDbRepository;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.RunningContinuousIntegrationCondition;

import lombok.Getter;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CouchDbThrottledSubmissionHandlerInterceptorAdapterTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Category(CouchDbCategory.class)
@SpringBootTest(classes = {
    CasCouchDbThrottlingConfiguration.class,
    CasCouchDbCoreConfiguration.class,
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
    CasSupportCouchDbAuditConfiguration.class,
    CasCoreWebConfiguration.class,
    CasRegisteredServicesTestConfiguration.class,
    CasThrottlingConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class})
@TestPropertySource(properties = {
    "cas.audit.couchDb.dbName=throttle",
    "cas.audit.couchDb.asynchronous=false",
    "cas.audit.couchDb.username=cas",
    "cas.audit.couchdb.password=password"
})
@Getter
@ConditionalIgnore(condition = RunningContinuousIntegrationCondition.class)
public class CouchDbThrottledSubmissionHandlerInterceptorAdapterTests extends
    BaseThrottledSubmissionHandlerInterceptorAdapterTests {

    @Autowired
    @Qualifier("authenticationThrottle")
    private ThrottledSubmissionHandlerInterceptor throttle;

    @Autowired
    @Qualifier("auditActionContextCouchDbRepository")
    private AuditActionContextCouchDbRepository couchDbRepository;

    @Autowired
    @Qualifier("auditCouchDbFactory")
    private CouchDbConnectorFactory couchDbFactory;

    @BeforeEach
    public void setUp() {
        couchDbFactory.getCouchDbInstance().createDatabaseIfNotExists(couchDbFactory.getCouchDbConnector().getDatabaseName());
        couchDbRepository.initStandardDesignDocument();
    }

    @AfterEach
    public void tearDown() {
        couchDbFactory.getCouchDbInstance().deleteDatabase(couchDbFactory.getCouchDbConnector().getDatabaseName());
    }
}
