package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.sms.TwillioSmsSender;
import org.apereo.cas.util.io.SmsSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

/**
 * This is {@link TwillioSmsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("twillioSmsConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class TwillioSmsConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;
    
    @Bean
    public SmsSender smsSender() {
        Assert.notNull(casProperties.getTwillio().getAccountId(), "Twillio account id cannot be blank");
        Assert.notNull(casProperties.getTwillio().getToken(), "Twillio token cannot be blank");
        
        return new TwillioSmsSender(casProperties.getTwillio().getAccountId(),
                casProperties.getTwillio().getToken());
    }
}
