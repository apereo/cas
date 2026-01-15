package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.notifications.sms.SmsSender;
import org.apereo.cas.support.sms.SmsModeSmsSender;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasSmsModeSmsAutoConfiguration}.
 *
 * @author Jérôme Rautureau
 * @since 6.5.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Notifications, module = "smsmode")
@AutoConfiguration
public class CasSmsModeSmsAutoConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public SmsSender smsSender(final CasConfigurationProperties casProperties) {
        val smsMode = casProperties.getSmsProvider().getSmsMode();
        return new SmsModeSmsSender(smsMode);
    }
}
