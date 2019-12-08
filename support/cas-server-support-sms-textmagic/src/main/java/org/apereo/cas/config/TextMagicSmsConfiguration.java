package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.sms.TextMagicSmsSender;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.io.SmsSender;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

/**
 * This is {@link TextMagicSmsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration(value = "textMagicSmsConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class TextMagicSmsConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("noRedirectHttpClient")
    private ObjectProvider<HttpClient> httpClient;

    @Bean
    @RefreshScope
    public SmsSender smsSender() {
        val textMagic = casProperties.getSmsProvider().getTextMagic();
        return new TextMagicSmsSender(textMagic, Optional.of(httpClient.getObject()));
    }
}
