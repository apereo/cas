package org.apereo.cas.config;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.sms.TextMagicSmsSender;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.io.SmsSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link TextMagicSmsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("textMagicSmsConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class TextMagicSmsConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("noRedirectHttpClient")
    private HttpClient httpClient;


    @Bean
    public SmsSender smsSender() {
        final var textMagic = casProperties.getSmsProvider().getTextMagic();
        return new TextMagicSmsSender(textMagic.getUsername(), textMagic.getToken(),
            textMagic.getUrl(), httpClient);
    }
}
