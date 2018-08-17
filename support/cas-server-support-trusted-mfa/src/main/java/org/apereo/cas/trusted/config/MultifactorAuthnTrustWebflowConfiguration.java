package org.apereo.cas.trusted.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.web.flow.MultifactorAuthenticationSetTrustAction;
import org.apereo.cas.trusted.web.flow.MultifactorAuthenticationVerifyTrustAction;
import org.apereo.cas.trusted.web.flow.fingerprint.DeviceFingerprintStrategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.execution.Action;

import static org.apereo.cas.trusted.BeanNames.BEAN_DEVICE_FINGERPRINT_STRATEGY;

/**
 * This is {@link MultifactorAuthnTrustWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("multifactorAuthnTrustWebflowConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class MultifactorAuthnTrustWebflowConfiguration {

    @Autowired
    @Qualifier(BEAN_DEVICE_FINGERPRINT_STRATEGY)
    private DeviceFingerprintStrategy deviceFingerprintStrategy;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("mfaTrustEngine")
    private MultifactorAuthenticationTrustStorage mfaTrustEngine;

    @Bean
    public Action mfaSetTrustAction() {
        return new MultifactorAuthenticationSetTrustAction(mfaTrustEngine,
            deviceFingerprintStrategy,
            casProperties.getAuthn().getMfa().getTrusted());
    }

    @Bean
    public Action mfaVerifyTrustAction() {
        return new MultifactorAuthenticationVerifyTrustAction(mfaTrustEngine,
            deviceFingerprintStrategy,
            casProperties.getAuthn().getMfa().getTrusted());
    }
}
