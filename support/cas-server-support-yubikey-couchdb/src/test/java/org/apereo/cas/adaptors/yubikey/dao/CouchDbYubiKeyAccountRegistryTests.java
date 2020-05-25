package org.apereo.cas.adaptors.yubikey.dao;

import org.apereo.cas.adaptors.yubikey.AbstractYubiKeyAccountRegistryTests;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
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
import org.apereo.cas.config.support.authentication.YubiKeyAuthenticationMultifactorProviderBypassConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.couchdb.yubikey.YubiKeyAccountCouchDbRepository;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.services.web.config.CasThemesConfiguration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasMultifactorAuthenticationWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasWebflowContextConfiguration;

import lombok.Getter;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
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
@Tag("CouchDb")
@SpringBootTest(classes = {
    CouchDbYubiKeyConfiguration.class,
    YubiKeyAuthenticationMultifactorProviderBypassConfiguration.class,
    CasCouchDbCoreConfiguration.class,
    AbstractYubiKeyAccountRegistryTests.YubiKeyAccountRegistryTestConfiguration.class,
    YubiKeyAuthenticationEventExecutionPlanConfiguration.class,
    YubiKeyConfiguration.class,
    CasCoreMultifactorAuthenticationConfiguration.class,
    CasMultifactorAuthenticationWebflowConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasWebflowContextConfiguration.class,
    CasCoreHttpConfiguration.class,
    AopAutoConfiguration.class,
    CasThemesConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreWebflowConfiguration.class,
    CasCoreConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCookieConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreWebConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasDefaultServiceTicketIdGeneratorsConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    RefreshAutoConfiguration.class
},
    properties = {
        "cas.authn.mfa.yubikey.clientId=18423",
        "cas.authn.mfa.yubikey.secretKey=zAIqhjui12mK8x82oe9qzBEb0As=",
        "cas.authn.mfa.yubikey.couch-db.username=cas",
        "cas.authn.mfa.yubikey.couch-db.password=password"
    })
@Getter
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnabledIfPortOpen(port = 5984)
public class CouchDbYubiKeyAccountRegistryTests extends AbstractYubiKeyAccountRegistryTests {
    @Autowired
    @Qualifier("yubiKeyAccountRegistry")
    private YubiKeyAccountRegistry yubiKeyAccountRegistry;

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
