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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link MultifactorAuthnTrustWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "multifactorAuthnTrustWebflowConfiguration", proxyBeanMethods = false)
public class MultifactorAuthnTrustWebflowConfiguration {

    @ConditionalOnMissingBean(name = "mfaTrustedDeviceBypassEvaluator")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MultifactorAuthenticationTrustedDeviceBypassEvaluator mfaTrustedDeviceBypassEvaluator(
        @Qualifier("registeredServiceAccessStrategyEnforcer")
        final AuditableExecution registeredServiceAccessStrategyEnforcer) {
        return new DefaultMultifactorAuthenticationTrustedDeviceBypassEvaluator(registeredServiceAccessStrategyEnforcer);
    }

    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_MFA_SET_TRUST_ACTION)
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public Action mfaSetTrustAction(final CasConfigurationProperties casProperties,
                                    @Qualifier("mfaTrustedDeviceBypassEvaluator")
                                    final MultifactorAuthenticationTrustedDeviceBypassEvaluator mfaTrustedDeviceBypassEvaluator,
                                    @Qualifier("deviceFingerprintStrategy")
                                    final DeviceFingerprintStrategy deviceFingerprintStrategy,
                                    @Qualifier("mfaTrustEngine")
                                    final MultifactorAuthenticationTrustStorage mfaTrustEngine,
                                    @Qualifier("registeredServiceAccessStrategyEnforcer")
                                    final AuditableExecution registeredServiceAccessStrategyEnforcer) {
        return new MultifactorAuthenticationSetTrustAction(mfaTrustEngine, deviceFingerprintStrategy, casProperties.getAuthn().getMfa().getTrusted(), registeredServiceAccessStrategyEnforcer,
            mfaTrustedDeviceBypassEvaluator);
    }

    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_MFA_VERIFY_TRUST_ACTION)
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public Action mfaVerifyTrustAction(final CasConfigurationProperties casProperties,
                                       @Qualifier("mfaTrustedDeviceBypassEvaluator")
                                       final MultifactorAuthenticationTrustedDeviceBypassEvaluator mfaTrustedDeviceBypassEvaluator,
                                       @Qualifier("deviceFingerprintStrategy")
                                       final DeviceFingerprintStrategy deviceFingerprintStrategy,
                                       @Qualifier("mfaTrustEngine")
                                       final MultifactorAuthenticationTrustStorage mfaTrustEngine,
                                       @Qualifier("registeredServiceAccessStrategyEnforcer")
                                       final AuditableExecution registeredServiceAccessStrategyEnforcer) {
        return new MultifactorAuthenticationVerifyTrustAction(mfaTrustEngine, deviceFingerprintStrategy, casProperties.getAuthn().getMfa().getTrusted(),
            registeredServiceAccessStrategyEnforcer, mfaTrustedDeviceBypassEvaluator);
    }

    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_MFA_PREPARE_TRUST_DEVICE_VIEW_ACTION)
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public Action mfaPrepareTrustDeviceViewAction(final CasConfigurationProperties casProperties,
                                                  @Qualifier("mfaTrustedDeviceBypassEvaluator")
                                                  final MultifactorAuthenticationTrustedDeviceBypassEvaluator mfaTrustedDeviceBypassEvaluator,
                                                  @Qualifier("deviceFingerprintStrategy")
                                                  final DeviceFingerprintStrategy deviceFingerprintStrategy,
                                                  @Qualifier("mfaTrustDeviceNamingStrategy")
                                                  final MultifactorAuthenticationTrustedDeviceNamingStrategy mfaTrustDeviceNamingStrategy,
                                                  @Qualifier("mfaTrustEngine")
                                                  final MultifactorAuthenticationTrustStorage mfaTrustEngine,
                                                  @Qualifier("registeredServiceAccessStrategyEnforcer")
                                                  final AuditableExecution registeredServiceAccessStrategyEnforcer,
                                                  @Qualifier(ServicesManager.BEAN_NAME)
                                                  final ServicesManager servicesManager) {
        return new MultifactorAuthenticationPrepareTrustDeviceViewAction(mfaTrustEngine, deviceFingerprintStrategy, casProperties.getAuthn().getMfa().getTrusted(),
            registeredServiceAccessStrategyEnforcer, servicesManager, mfaTrustedDeviceBypassEvaluator, mfaTrustDeviceNamingStrategy);
    }
}
