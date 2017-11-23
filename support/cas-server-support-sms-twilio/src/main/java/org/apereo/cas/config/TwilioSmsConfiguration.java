package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.sms.TwilioSmsSender;
import org.apereo.cas.util.io.SmsSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

/**
 * This is {@link TwilioSmsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("twilioSmsConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class TwilioSmsConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;
    
    @Bean
    public SmsSender smsSender() {
        Assert.notNull(casProperties.getTwilio().getAccountId(), "Twilio account id cannot be blank");
        Assert.notNull(casProperties.getTwilio().getToken(), "Twilio token cannot be blank");
        return new TwilioSmsSender(casProperties.getTwilio().getAccountId(), casProperties.getTwilio().getToken());
    }
}
