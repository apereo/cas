package org.apereo.cas.adaptors.yubikey.dao;

import org.apereo.cas.adaptors.yubikey.BaseYubiKeyAccountRegistryTests;
import org.apereo.cas.category.CouchDbCategory;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasCouchDbCoreConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CouchDbYubiKeyConfiguration;
import org.apereo.cas.config.YubiKeyConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.config.support.authentication.YubiKeyAuthenticationEventExecutionPlanConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.couchdb.yubikey.YubiKeyAccountCouchDbRepository;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.services.web.config.CasThemesConfiguration;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasWebflowContextConfiguration;

import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

/**
 * This is {@link CouchDbYubiKeyAccountRegistryTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Category(CouchDbCategory.class)
@SpringBootTest(
    classes = {
        CouchDbYubiKeyConfiguration.class,
        CasCouchDbCoreConfiguration.class,
        BaseYubiKeyAccountRegistryTests.YubiKeyAccountRegistryTestConfiguration.class,
        YubiKeyAuthenticationEventExecutionPlanConfiguration.class,
        YubiKeyConfiguration.class,
        CasCoreServicesConfiguration.class,
        CasWebflowContextConfiguration.class,
        CasCoreHttpConfiguration.class,
        AopAutoConfiguration.class,
        CasThemesConfiguration.class,
        CasCoreTicketsConfiguration.class,
        CasCoreLogoutConfiguration.class,
        CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
        CasCoreAuthenticationPrincipalConfiguration.class,
        CasCoreWebflowConfiguration.class,
        CasCoreConfiguration.class,
        CasPersonDirectoryTestConfiguration.class,
        CasCoreAuthenticationSupportConfiguration.class,
        CasCookieConfiguration.class,
        CasCoreUtilConfiguration.class,
        CasCoreWebConfiguration.class,
        CasCoreHttpConfiguration.class,
        CasCoreTicketCatalogConfiguration.class,
        CasDefaultServiceTicketIdGeneratorsConfiguration.class,
        CasWebApplicationServiceFactoryConfiguration.class,
        RefreshAutoConfiguration.class
    },
    properties = {
        "cas.authn.mfa.yubikey.clientId=18423",
        "cas.authn.mfa.yubikey.secretKey=zAIqhjui12mK8x82oe9qzBEb0As=",
        "cas.authn.mfa.yubikey.couchDb.username=cas",
        "cas.authn.mfa.yubikey.couchdb.password=password"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CouchDbYubiKeyAccountRegistryTests extends BaseYubiKeyAccountRegistryTests {
    @Autowired
    @Qualifier("yubikeyCouchDbConnector")
    private CouchDbConnector couchDbConnector;

    @Autowired
    @Qualifier("yubikeyCouchDbInstance")
    private CouchDbInstance couchDbInstance;

    @Autowired
    @Qualifier("couchDbYubiKeyAccountRepository")
    private YubiKeyAccountCouchDbRepository couchDbRepository;

    @BeforeEach
    public void setUp() {
        couchDbInstance.createDatabaseIfNotExists(couchDbConnector.getDatabaseName());
        couchDbRepository.initStandardDesignDocument();
    }

    @AfterEach
    public void tearDown() {
        couchDbInstance.deleteDatabase(couchDbConnector.getDatabaseName());
    }
}
