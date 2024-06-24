package org.apereo.cas.support.oauth.web.flow;

import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasOAuth20AutoConfiguration;
import org.apereo.cas.config.CasOAuth20WebflowAutoConfiguration;
import org.apereo.cas.config.CasThemesAutoConfiguration;
import org.apereo.cas.config.CasThymeleafAutoConfiguration;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
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
        WebMvcAutoConfiguration.class,
        AopAutoConfiguration.class,
        CasThemesAutoConfiguration.class,
        CasThymeleafAutoConfiguration.class,
        CasCoreAutoConfiguration.class,
        CasOAuth20AutoConfiguration.class,
        CasOAuth20WebflowAutoConfiguration.class})
    @SpringBootConfiguration
    @Import(BaseWebflowConfigurerTests.SharedTestConfiguration.class)
    public static class SharedTestConfiguration {
    }
}
