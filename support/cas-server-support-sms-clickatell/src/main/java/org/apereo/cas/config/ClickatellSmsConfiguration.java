package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.notifications.sms.SmsSender;
import org.apereo.cas.support.sms.ClickatellSmsSender;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link ClickatellSmsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration(value = "clickatellSmsConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class ClickatellSmsConfiguration {

    @Bean
    @Autowired
    public SmsSender smsSender(final CasConfigurationProperties casProperties) {
        val clickatell = casProperties.getSmsProvider().getClickatell();
        return new ClickatellSmsSender(clickatell.getToken(), clickatell.getServerUrl());
    }
}
