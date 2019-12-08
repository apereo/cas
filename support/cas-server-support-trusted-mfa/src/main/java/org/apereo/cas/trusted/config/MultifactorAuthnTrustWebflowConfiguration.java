package org.apereo.cas.trusted.config;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.trusted.authentication.DefaultMultifactorAuthenticationTrustedDeviceBypassEvaluator;
import org.apereo.cas.trusted.authentication.MultifactorAuthenticationTrustedDeviceBypassEvaluator;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.web.flow.MultifactorAuthenticationPrepareTrustDeviceViewAction;
import org.apereo.cas.trusted.web.flow.MultifactorAuthenticationSetTrustAction;
import org.apereo.cas.trusted.web.flow.MultifactorAuthenticationVerifyTrustAction;
import org.apereo.cas.trusted.web.flow.fingerprint.DeviceFingerprintStrategy;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
    private ObjectProvider<DeviceFingerprintStrategy> deviceFingerprintStrategy;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("mfaTrustEngine")
    private ObjectProvider<MultifactorAuthenticationTrustStorage> mfaTrustEngine;

    @Autowired
    @Qualifier("registeredServiceAccessStrategyEnforcer")
    private ObjectProvider<AuditableExecution> registeredServiceAccessStrategyEnforcer;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @ConditionalOnMissingBean(name = "mfaTrustedDeviceBypassEvaluator")
    @Bean
    public MultifactorAuthenticationTrustedDeviceBypassEvaluator mfaTrustedDeviceBypassEvaluator() {
        return new DefaultMultifactorAuthenticationTrustedDeviceBypassEvaluator(registeredServiceAccessStrategyEnforcer.getObject());
    }
    
    @ConditionalOnMissingBean(name = "mfaSetTrustAction")
    @Bean
    public Action mfaSetTrustAction() {
        return new MultifactorAuthenticationSetTrustAction(mfaTrustEngine.getObject(),
            deviceFingerprintStrategy.getObject(),
            casProperties.getAuthn().getMfa().getTrusted(),
            registeredServiceAccessStrategyEnforcer.getObject(),
            mfaTrustedDeviceBypassEvaluator());
    }

    @ConditionalOnMissingBean(name = "mfaVerifyTrustAction")
    @Bean
    public Action mfaVerifyTrustAction() {
        return new MultifactorAuthenticationVerifyTrustAction(mfaTrustEngine.getObject(),
            deviceFingerprintStrategy.getObject(),
            casProperties.getAuthn().getMfa().getTrusted(),
            registeredServiceAccessStrategyEnforcer.getObject(),
            mfaTrustedDeviceBypassEvaluator());
    }

    @ConditionalOnMissingBean(name = "mfaPrepareTrustDeviceViewAction")
    @Bean
    public Action mfaPrepareTrustDeviceViewAction() {
        return new MultifactorAuthenticationPrepareTrustDeviceViewAction(mfaTrustEngine.getObject(),
            deviceFingerprintStrategy.getObject(),
            casProperties.getAuthn().getMfa().getTrusted(),
            registeredServiceAccessStrategyEnforcer.getObject(),
            servicesManager.getObject(),
            mfaTrustedDeviceBypassEvaluator());
    }
}
