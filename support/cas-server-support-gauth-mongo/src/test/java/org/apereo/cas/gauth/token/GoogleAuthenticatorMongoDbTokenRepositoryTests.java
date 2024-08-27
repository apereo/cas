package org.apereo.cas.gauth.token;

import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.config.CasGoogleAuthenticatorAutoConfiguration;
import org.apereo.cas.config.CasGoogleAuthenticatorMongoDbAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * This is {@link GoogleAuthenticatorMongoDbTokenRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Tag("MongoDbMFA")
@ExtendWith(CasTestExtension.class)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasGoogleAuthenticatorMongoDbAutoConfiguration.class,
    CasCoreMultifactorAuthenticationAutoConfiguration.class,
    CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreCookieAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasPersonDirectoryAutoConfiguration.class,
    CasGoogleAuthenticatorAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreScriptingAutoConfiguration.class,
    CasCoreWebflowAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class},
    properties = {
        "cas.authn.mfa.gauth.mongo.user-id=root",
        "cas.authn.mfa.gauth.mongo.password=secret",
        "cas.authn.mfa.gauth.mongo.host=localhost",
        "cas.authn.mfa.gauth.mongo.port=27017",
        "cas.authn.mfa.gauth.mongo.authentication-database-name=admin",
        "cas.authn.mfa.gauth.mongo.drop-collection=true",
        "cas.authn.mfa.gauth.mongo.database-name=gauth-token",
        "cas.authn.mfa.gauth.crypto.enabled=false"
    })
@EnableTransactionManagement(proxyTargetClass = false)
@EnableAspectJAutoProxy(proxyTargetClass = false)
@EnableScheduling
@Getter
@EnabledIfListeningOnPort(port = 27017)
class GoogleAuthenticatorMongoDbTokenRepositoryTests extends BaseOneTimeTokenRepositoryTests {
}
