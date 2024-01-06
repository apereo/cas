package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.notifications.sms.SmsSender;
import org.apereo.cas.support.sms.TextMagicSmsSender;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import java.util.Optional;

/**
 * This is {@link CasTextMagicSmsAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Notifications, module = "textmagic")
@AutoConfiguration
public class CasTextMagicSmsAutoConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public SmsSender smsSender(final CasConfigurationProperties casProperties,
                               @Qualifier(HttpClient.BEAN_NAME_HTTPCLIENT)
                               final HttpClient httpClient) {
        val textMagic = casProperties.getSmsProvider().getTextMagic();
        return new TextMagicSmsSender(textMagic, Optional.of(httpClient));
    }
}
