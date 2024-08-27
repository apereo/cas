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
import org.apereo.cas.config.CasGoogleAuthenticatorJpaAutoConfiguration;
import org.apereo.cas.config.CasHibernateJpaAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * This is {@link GoogleAuthenticatorJpaTokenRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasGoogleAuthenticatorJpaAutoConfiguration.class,
    CasGoogleAuthenticatorAutoConfiguration.class,
    CasHibernateJpaAutoConfiguration.class,
    CasCoreCookieAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasPersonDirectoryAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreMultifactorAuthenticationAutoConfiguration.class,
    CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
    CasCoreWebflowAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreScriptingAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class
}, properties = "cas.jdbc.show-sql=true"
)
@EnableTransactionManagement(proxyTargetClass = false)
@EnableAspectJAutoProxy(proxyTargetClass = false)
@EnableScheduling
@Getter
@Tag("JDBCMFA")
@ExtendWith(CasTestExtension.class)
@EnableRetry
class GoogleAuthenticatorJpaTokenRepositoryTests extends BaseOneTimeTokenRepositoryTests {
}
