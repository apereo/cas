package org.apereo.cas.web.flow;

import org.apereo.cas.api.PasswordlessAuthenticationPreProcessor;
import org.apereo.cas.config.CasAuthenticationEventExecutionPlanTestConfiguration;
import org.apereo.cas.config.CasCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreAuditAutoConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.CasWebflowAutoConfiguration;
import org.apereo.cas.config.CoreSamlAutoConfiguration;
import org.apereo.cas.config.DelegatedAuthenticationAutoConfiguration;
import org.apereo.cas.config.PasswordlessAuthenticationAutoConfiguration;
import org.apereo.cas.config.PasswordlessAuthenticationWebflowAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * This is {@link BasePasswordlessAuthenticationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    MailSenderAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    MockMvcAutoConfiguration.class,
    ErrorMvcAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    DelegatedAuthenticationAutoConfiguration.class,
    CasWebflowAutoConfiguration.class,
    CoreSamlAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasCookieAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreAuditAutoConfiguration.class,
    CasCoreMultifactorAuthenticationAutoConfiguration.class,
    CasMultifactorAuthenticationWebflowAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasRegisteredServicesTestConfiguration.class,
    CasAuthenticationEventExecutionPlanTestConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    PasswordlessAuthenticationAutoConfiguration.class,
    PasswordlessAuthenticationWebflowAutoConfiguration.class
})
public abstract class BasePasswordlessAuthenticationActionTests {
    @Autowired
    protected ConfigurableApplicationContext applicationContext;

    @TestConfiguration(value = "AuthenticationTestConfiguration", proxyBeanMethods = false)
    static class AuthenticationTestConfiguration {
        @Bean
        public PasswordlessAuthenticationPreProcessor testPasswordlessAuthenticationPreProcessor() {
            return (builder, principal, service, credential, token) -> builder;
        }
    }
}
