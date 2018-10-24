package org.apereo.cas.gauth.credential;

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
import org.apereo.cas.config.GoogleAuthenticatorMongoDbConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.config.support.EnvironmentConversionServiceInitializer;
import org.apereo.cas.config.support.authentication.GoogleAuthenticatorAuthenticationEventExecutionPlanConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.SchedulingUtils;
import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.RunningContinuousIntegrationCondition;

import lombok.val;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import static org.junit.Assert.*;

/**
 * This is {@link MongoDbGoogleAuthenticatorTokenCredentialRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@SpringBootTest(classes = {
    MongoDbGoogleAuthenticatorTokenCredentialRepositoryTests.MongoTestConfiguration.class,
    GoogleAuthenticatorMongoDbConfiguration.class,
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
    CasCoreWebConfiguration.class})
@EnableTransactionManagement(proxyTargetClass = true)
@EnableAspectJAutoProxy(proxyTargetClass = true)
@TestPropertySource(properties = {
    "cas.authn.mfa.gauth.mongo.host=localhost",
    "cas.authn.mfa.gauth.mongo.port=27017",
    "cas.authn.mfa.gauth.mongo.dropCollection=true",
    "cas.authn.mfa.gauth.mongo.userId=root",
    "cas.authn.mfa.gauth.mongo.password=secret",
    "cas.authn.mfa.gauth.mongo.authenticationDatabaseName=admin",
    "cas.authn.mfa.gauth.mongo.databaseName=gauth-token-credential",
    "cas.authn.mfa.gauth.crypto.enabled=false"
})
@EnableScheduling
@ContextConfiguration(initializers = EnvironmentConversionServiceInitializer.class)
@Category(MongoDbCategory.class)
@ConditionalIgnore(condition = RunningContinuousIntegrationCondition.class)
public class MongoDbGoogleAuthenticatorTokenCredentialRepositoryTests {
    @Autowired
    @Qualifier("googleAuthenticatorAccountRegistry")
    private OneTimeTokenCredentialRepository registry;

    @BeforeEach
    public void cleanUp() {
        registry.deleteAll();
    }

    @Test
    public void verifySave() {
        registry.save("uid", "secret", 143211, CollectionUtils.wrapList(1, 2, 3, 4, 5, 6));
        val s = registry.get("uid");
        assertEquals("secret", s.getSecretKey());
        val c = registry.load();
        assertFalse(c.isEmpty());
    }

    @Test
    public void verifySaveAndUpdate() {
        registry.save("casuser", "secret", 222222, CollectionUtils.wrapList(1, 2, 3, 4, 5, 6));
        val s = registry.get("casuser");
        assertNotNull(s.getRegistrationDate());
        assertEquals(222222, s.getValidationCode());
        s.setSecretKey("newSecret");
        s.setValidationCode(999666);
        registry.update(s);
        val s2 = registry.get("casuser");
        assertEquals(999666, s2.getValidationCode());
        assertEquals("newSecret", s2.getSecretKey());
    }

    @TestConfiguration
    public static class MongoTestConfiguration implements InitializingBean {
        @Autowired
        protected ApplicationContext applicationContext;

        @Override
        public void afterPropertiesSet() {
            SchedulingUtils.prepScheduledAnnotationBeanPostProcessor(applicationContext);
        }
    }
}
