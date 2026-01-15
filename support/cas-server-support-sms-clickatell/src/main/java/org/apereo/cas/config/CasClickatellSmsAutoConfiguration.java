package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.notifications.sms.SmsSender;
import org.apereo.cas.support.sms.ClickatellSmsSender;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasClickatellSmsAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Notifications, module = "clickatell")
@AutoConfiguration
public class CasClickatellSmsAutoConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public SmsSender smsSender(final CasConfigurationProperties casProperties) {
        val clickatell = casProperties.getSmsProvider().getClickatell();
        return new ClickatellSmsSender(clickatell.getToken(), SpringExpressionLanguageValueResolver.getInstance().resolve(clickatell.getServerUrl()));
    }
}
