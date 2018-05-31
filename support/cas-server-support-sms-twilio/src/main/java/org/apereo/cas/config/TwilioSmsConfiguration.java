package org.apereo.cas.config;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class TwilioSmsConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;
    
    @Bean
    public SmsSender smsSender() {
        final var twilio = casProperties.getSmsProvider().getTwilio();
        Assert.notNull(twilio.getAccountId(), "Twilio account id cannot be blank");
        Assert.notNull(twilio.getToken(), "Twilio token cannot be blank");
        return new TwilioSmsSender(twilio.getAccountId(), twilio.getToken());
    }
}
