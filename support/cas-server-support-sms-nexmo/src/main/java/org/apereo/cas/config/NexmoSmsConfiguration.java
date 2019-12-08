package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.sms.NexmoSmsSender;
import org.apereo.cas.util.io.SmsSender;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link NexmoSmsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Configuration(value = "nexmoSmsConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class NexmoSmsConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    public SmsSender smsSender() {
        val nexmo = casProperties.getSmsProvider().getNexmo();
        return new NexmoSmsSender(nexmo);
    }
}
