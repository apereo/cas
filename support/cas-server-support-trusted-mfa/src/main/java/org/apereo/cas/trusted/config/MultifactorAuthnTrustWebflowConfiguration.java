package org.apereo.cas.trusted.config;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.trusted.authentication.DefaultMultifactorAuthenticationTrustedDeviceBypassEvaluator;
import org.apereo.cas.trusted.authentication.MultifactorAuthenticationTrustedDeviceBypassEvaluator;
import org.apereo.cas.trusted.authentication.MultifactorAuthenticationTrustedDeviceNamingStrategy;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.web.flow.MultifactorAuthenticationPrepareTrustDeviceViewAction;
import org.apereo.cas.trusted.web.flow.MultifactorAuthenticationSetTrustAction;
import org.apereo.cas.trusted.web.flow.MultifactorAuthenticationVerifyTrustAction;
import org.apereo.cas.trusted.web.flow.fingerprint.DeviceFingerprintStrategy;
import org.apereo.cas.web.flow.CasWebflowConstants;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.execution.Action;

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
    @Qualifier(DeviceFingerprintStrategy.DEFAULT_BEAN_NAME)
    private ObjectProvider<DeviceFingerprintStrategy> deviceFingerprintStrategy;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("mfaTrustDeviceNamingStrategy")
    private ObjectProvider<MultifactorAuthenticationTrustedDeviceNamingStrategy> mfaTrustDeviceNamingStrategy;

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
    @RefreshScope
    public MultifactorAuthenticationTrustedDeviceBypassEvaluator mfaTrustedDeviceBypassEvaluator() {
        return new DefaultMultifactorAuthenticationTrustedDeviceBypassEvaluator(registeredServiceAccessStrategyEnforcer.getObject());
    }
    
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_MFA_SET_TRUST_ACTION)
    @Bean
    @RefreshScope
    public Action mfaSetTrustAction() {
        return new MultifactorAuthenticationSetTrustAction(mfaTrustEngine.getObject(),
            deviceFingerprintStrategy.getObject(),
            casProperties.getAuthn().getMfa().getTrusted(),
            registeredServiceAccessStrategyEnforcer.getObject(),
            mfaTrustedDeviceBypassEvaluator());
    }

    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_MFA_VERIFY_TRUST_ACTION)
    @Bean
    @RefreshScope
    public Action mfaVerifyTrustAction() {
        return new MultifactorAuthenticationVerifyTrustAction(mfaTrustEngine.getObject(),
            deviceFingerprintStrategy.getObject(),
            casProperties.getAuthn().getMfa().getTrusted(),
            registeredServiceAccessStrategyEnforcer.getObject(),
            mfaTrustedDeviceBypassEvaluator());
    }

    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_MFA_PREPARE_TRUST_DEVICE_VIEW_ACTION)
    @Bean
    @RefreshScope
    public Action mfaPrepareTrustDeviceViewAction() {
        return new MultifactorAuthenticationPrepareTrustDeviceViewAction(mfaTrustEngine.getObject(),
            deviceFingerprintStrategy.getObject(),
            casProperties.getAuthn().getMfa().getTrusted(),
            registeredServiceAccessStrategyEnforcer.getObject(),
            servicesManager.getObject(),
            mfaTrustedDeviceBypassEvaluator(),
            mfaTrustDeviceNamingStrategy.getObject());
    }
}
