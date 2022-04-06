package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.CasFeatureModule;
import org.apereo.cas.notifications.sms.SmsSender;
import org.apereo.cas.support.sms.NexmoSmsSender;
import org.apereo.cas.util.spring.boot.ConditionalOnFeature;

import lombok.val;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link NexmoSmsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Configuration(value = "NexmoSmsConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeature(feature = CasFeatureModule.FeatureCatalog.Notifications, module = "nexmo")
public class NexmoSmsConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public SmsSender smsSender(final CasConfigurationProperties casProperties) {
        val nexmo = casProperties.getSmsProvider().getNexmo();
        return new NexmoSmsSender(nexmo);
    }
}
