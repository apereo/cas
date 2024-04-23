package org.apereo.cas.web;

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
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.config.CasThymeleafAutoConfiguration;
import org.apereo.cas.pac4j.client.DelegatedClientAuthenticationRequestCustomizer;
import org.apereo.cas.support.pac4j.authentication.clients.DelegatedAuthenticationClientsTestConfiguration;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.web.flow.DelegatedClientWebflowCustomizer;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * This is {@link BaseDelegatedAuthenticationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public abstract class BaseDelegatedAuthenticationTests {

    @ImportAutoConfiguration({
        RefreshAutoConfiguration.class,
        WebMvcAutoConfiguration.class,
        ThymeleafAutoConfiguration.class,
        MockMvcAutoConfiguration.class,
        ErrorMvcAutoConfiguration.class,
        AopAutoConfiguration.class
    })
    @SpringBootConfiguration
    @EnableWebMvc
    @Import({
        DelegatedAuthenticationWebflowTestConfiguration.class,
        DelegatedAuthenticationClientsTestConfiguration.class,
        CasDelegatedAuthenticationAutoConfiguration.class,
        CasCoreTicketsAutoConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasCoreSamlAutoConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasCoreServicesAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreWebflowAutoConfiguration.class,
        CasCoreMultifactorAuthenticationAutoConfiguration.class,
        CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasPersonDirectoryAutoConfiguration.class,
        CasCoreCookieAutoConfiguration.class,
        CasThymeleafAutoConfiguration.class,
        CasCoreAutoConfiguration.class,
        CasCoreAuditAutoConfiguration.class
    })
    public static class SharedTestConfiguration {
    }

    @TestConfiguration(value = "DelegatedAuthenticationWebflowTestConfiguration", proxyBeanMethods = false)
    static class DelegatedAuthenticationWebflowTestConfiguration {
        @Bean
        public DelegatedClientWebflowCustomizer surrogateCasMultifactorWebflowCustomizer() {
            return BeanSupplier.of(DelegatedClientWebflowCustomizer.class)
                .otherwiseProxy().get();
        }

        @Bean
        public DelegatedClientAuthenticationRequestCustomizer testDelegatedClientAuthenticationRequestCustomizer() {
            return BeanSupplier.of(DelegatedClientAuthenticationRequestCustomizer.class)
                .otherwiseProxy()
                .get();
        }
    }
}
