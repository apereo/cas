package org.apereo.cas.palantir.controller;

import module java.base;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.config.CasPalantirAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.config.CasThemesAutoConfiguration;
import org.apereo.cas.config.CasThymeleafAutoConfiguration;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;

/**
 * This is {@link BasePalantirTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
abstract class BasePalantirTests {
    @SpringBootTestAutoConfigurations
    @ImportAutoConfiguration({
        CasCoreTicketsAutoConfiguration.class,
        CasCoreServicesAutoConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasCoreAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasPersonDirectoryAutoConfiguration.class,
        CasThemesAutoConfiguration.class,
        CasThymeleafAutoConfiguration.class,
        CasCoreWebflowAutoConfiguration.class,
        CasCoreCookieAutoConfiguration.class,
        CasCoreMultifactorAuthenticationAutoConfiguration.class,
        CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasPalantirAutoConfiguration.class
    })
    @SpringBootConfiguration(proxyBeanMethods = false)
    public static class SharedTestConfiguration {
    }
}
