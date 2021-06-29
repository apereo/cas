package org.apereo.cas.support.oauth.web.flow;

import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasOAuth20AuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasOAuth20Configuration;
import org.apereo.cas.config.CasOAuth20ServicesConfiguration;
import org.apereo.cas.config.CasOAuth20WebflowConfiguration;
import org.apereo.cas.config.CasThymeleafConfiguration;
import org.apereo.cas.services.web.config.CasThemesConfiguration;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * This is {@link BaseOAuth20WebflowTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public abstract class BaseOAuth20WebflowTests {
    
    @ImportAutoConfiguration({
        RefreshAutoConfiguration.class,
        MailSenderAutoConfiguration.class,
        AopAutoConfiguration.class
    })
    @SpringBootConfiguration
    @Import({
        CasThemesConfiguration.class,
        CasThymeleafConfiguration.class,
        CasCoreConfiguration.class,
        CasOAuth20Configuration.class,
        CasOAuth20AuthenticationServiceSelectionStrategyConfiguration.class,
        CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
        CasOAuth20WebflowConfiguration.class,
        CasOAuth20ServicesConfiguration.class,
        BaseWebflowConfigurerTests.SharedTestConfiguration.class
    })
    public static class SharedTestConfiguration {
    }
}
