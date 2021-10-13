package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.notifications.sms.SmsSender;
import org.apereo.cas.support.sms.SmsModeSmsSender;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link SmsModeSmsConfiguration}.
 *
 * @author Jérôme Rautureau
 * @since 6.5.0
 */
@Configuration(value = "smsModeSmsConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SmsModeSmsConfiguration {

    @Bean
    @Autowired
    public SmsSender smsSender(final CasConfigurationProperties casProperties) {
        val smsMode = casProperties.getSmsProvider().getSmsMode();
        return new SmsModeSmsSender(smsMode);
    }
}
