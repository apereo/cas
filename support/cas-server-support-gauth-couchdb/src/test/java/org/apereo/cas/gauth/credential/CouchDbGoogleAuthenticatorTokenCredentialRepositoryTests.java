package org.apereo.cas.gauth.credential;

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
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.GoogleAuthenticatorCouchDbConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.config.support.EnvironmentConversionServiceInitializer;
import org.apereo.cas.config.support.authentication.GoogleAuthenticatorAuthenticationEventExecutionPlanConfiguration;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.couchdb.gauth.credential.GoogleAuthenticatorAccountCouchDbRepository;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;

import lombok.Getter;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.ContextConfiguration;

/**
 * This is {@link CouchDbGoogleAuthenticatorTokenCredentialRepositoryTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Category(CouchDbCategory.class)
@SpringBootTest(
    classes = {
        CasCouchDbCoreConfiguration.class,
        BaseOneTimeTokenCredentialRepositoryTests.BaseTestConfiguration.class,
        GoogleAuthenticatorCouchDbConfiguration.class,
        CasCoreTicketsConfiguration.class,
        CasCoreTicketCatalogConfiguration.class,
        CasCoreLogoutConfiguration.class,
        CasCoreHttpConfiguration.class,
        CasCoreServicesConfiguration.class,
        CasWebApplicationServiceFactoryConfiguration.class,
        CasCoreAuthenticationConfiguration.class,
        CasCoreServicesAuthenticationConfiguration.class,
        CasCoreAuthenticationMetadataConfiguration.class,
        CasCoreAuthenticationPolicyConfiguration.class,
        CasCoreAuthenticationPrincipalConfiguration.class,
        CasCoreAuthenticationHandlersConfiguration.class,
        CasCoreAuthenticationSupportConfiguration.class,
        CasPersonDirectoryConfiguration.class,
        GoogleAuthenticatorAuthenticationEventExecutionPlanConfiguration.class,
        AopAutoConfiguration.class,
        CasCoreConfiguration.class,
        CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
        CasCoreUtilConfiguration.class,
        RefreshAutoConfiguration.class,
        CasCoreWebConfiguration.class},
    properties = {
        "cas.authn.mfa.gauth.crypto.enabled=false",
        "cas.authn.mfa.gauth.couchDb.username=cas",
        "cas.authn.mfa.gauth.couchDb.dbName=gauth_credential",
        "cas.authn.mfa.gauth.couchdb.password=password"
    })
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableScheduling
@ContextConfiguration(initializers = EnvironmentConversionServiceInitializer.class)
@Getter
public class CouchDbGoogleAuthenticatorTokenCredentialRepositoryTests extends BaseOneTimeTokenCredentialRepositoryTests {

    @Autowired
    @Qualifier("oneTimeTokenAccountCouchDbFactory")
    private CouchDbConnectorFactory couchDbFactory;

    @Autowired
    @Qualifier("googleAuthenticatorAccountRegistry")
    private OneTimeTokenCredentialRepository registry;

    @Autowired
    @Qualifier("couchDbOneTimeTokenAccountRepository")
    private GoogleAuthenticatorAccountCouchDbRepository couchDbRepository;

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
