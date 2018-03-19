package org.apereo.cas.trusted.config;

import static org.apereo.cas.trusted.BeanNames.BEAN_DEVICE_FINGERPRINT_STRATEGY;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.trusteddevice.BaseDeviceFingerprintComponentProperties;
import org.apereo.cas.configuration.model.support.mfa.trusteddevice.DeviceFingerprintProperties;
import org.apereo.cas.trusted.web.flow.ClientIpDeviceFingerprintComponent;
import org.apereo.cas.trusted.web.flow.DefaultDeviceFingerprintStrategy;
import org.apereo.cas.trusted.web.flow.DeviceFingerprintComponent;
import org.apereo.cas.trusted.web.flow.DeviceFingerprintStrategy;
import org.apereo.cas.trusted.web.flow.UserAgentDeviceFingerprintComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration for {@link DefaultDeviceFingerprintStrategy}.
 *
 * @author Daniel Frett
 * @since 5.3.0
 */
@Configuration("multifactorAuthnTrustedDeviceFingerprintConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class MultifactorAuthnTrustedDeviceFingerprintConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    @RefreshScope
    public DeviceFingerprintComponent clientIpDeviceFingerprintComponent() {
        final BaseDeviceFingerprintComponentProperties properties =
                casProperties.getAuthn().getMfa().getTrusted().getDeviceFingerprint().getClientIp();
        if (properties.isEnabled()) {
            final ClientIpDeviceFingerprintComponent component = new ClientIpDeviceFingerprintComponent();
            component.setOrder(properties.getOrder());
            return component;
        } else {
            return DeviceFingerprintComponent.noOp();
        }
    }

    @Bean
    @RefreshScope
    public DeviceFingerprintComponent userAgentDeviceFingerprintComponent() {
        final BaseDeviceFingerprintComponentProperties properties =
                casProperties.getAuthn().getMfa().getTrusted().getDeviceFingerprint().getUserAgent();
        if (properties.isEnabled()) {
            final UserAgentDeviceFingerprintComponent component = new UserAgentDeviceFingerprintComponent();
            component.setOrder(1);
            return component;
        } else {
            return DeviceFingerprintComponent.noOp();
        }
    }

    @ConditionalOnMissingBean(name = BEAN_DEVICE_FINGERPRINT_STRATEGY)
    @Bean(BEAN_DEVICE_FINGERPRINT_STRATEGY)
    @RefreshScope
    public DeviceFingerprintStrategy deviceFingerprintStrategy(final List<DeviceFingerprintComponent> strategies) {
        final DeviceFingerprintProperties properties =
                casProperties.getAuthn().getMfa().getTrusted().getDeviceFingerprint();
        return new DefaultDeviceFingerprintStrategy(strategies, properties.getComponentSeparator());
    }
}
