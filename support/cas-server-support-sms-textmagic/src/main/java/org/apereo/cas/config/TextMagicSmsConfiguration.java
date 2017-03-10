package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.sms.TextMagicSmsSender;
import org.apereo.cas.util.io.SmsSender;
import org.springframework.beans.factory.annotation.Autowired;
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
public class TextMagicSmsConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;
    
    @Bean
    public SmsSender smsSender() {
        return new TextMagicSmsSender(
                casProperties.getTextMagic().getUsername(),
                casProperties.getTextMagic().getToken());
    }
}
