package org.apereo.cas.web.flow.action;

import org.apereo.cas.authentication.AcceptUsersAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.config.CasCoreAuditAutoConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreSamlAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.config.CasDelegatedAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasSurrogateAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasSurrogateAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasThemesAutoConfiguration;
import org.apereo.cas.util.CollectionUtils;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * This is {@link BaseSurrogateAuthenticationTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public abstract class BaseSurrogateAuthenticationTests {

    @ImportAutoConfiguration({
        WebMvcAutoConfiguration.class,
        MailSenderAutoConfiguration.class,
        AopAutoConfiguration.class,
        RefreshAutoConfiguration.class
    })
    @SpringBootConfiguration
    @Import({
        AuthenticationTestConfiguration.class,
        CasSurrogateAuthenticationAutoConfiguration.class,
        CasSurrogateAuthenticationWebflowAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasCoreServicesAutoConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasCoreAuditAutoConfiguration.class,
        CasCoreTicketsAutoConfiguration.class,
        CasCoreMultifactorAuthenticationAutoConfiguration.class,
        CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
        CasCoreWebflowAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasThemesAutoConfiguration.class,
        CasCoreAutoConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class,
        CasPersonDirectoryTestConfiguration.class,
        CasCoreCookieAutoConfiguration.class,
        CasCoreSamlAutoConfiguration.class,
        CasDelegatedAuthenticationAutoConfiguration.class
    })
    public static class SharedTestConfiguration {
    }

    @TestConfiguration(value = "AuthenticationTestConfiguration", proxyBeanMethods = false)
    static class AuthenticationTestConfiguration {
        @Bean
        public AuthenticationEventExecutionPlanConfigurer surrogateAuthenticationEventExecutionPlanConfigurer() {
            return plan -> plan.registerAuthenticationHandler(new AcceptUsersAuthenticationHandler(CollectionUtils.wrap("casuser", "Mellon")));
        }

    }
}
