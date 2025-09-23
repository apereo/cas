package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.adaptive.DefaultAdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.authentication.adaptive.intel.BlackDotIPAddressIntelligenceService;
import org.apereo.cas.authentication.adaptive.intel.DefaultIPAddressIntelligenceService;
import org.apereo.cas.authentication.adaptive.intel.GroovyIPAddressIntelligenceService;
import org.apereo.cas.authentication.adaptive.intel.IPAddressIntelligenceService;
import org.apereo.cas.authentication.adaptive.intel.RestfulIPAddressIntelligenceService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasCoreAuthenticationPolicyConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Authentication)
@Configuration(value = "CasCoreAuthenticationPolicyConfiguration", proxyBeanMethods = false)
class CasCoreAuthenticationPolicyConfiguration {

    @ConditionalOnMissingBean(name = IPAddressIntelligenceService.BEAN_NAME)
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public IPAddressIntelligenceService ipAddressIntelligenceService(
        @Qualifier(TenantExtractor.BEAN_NAME)
        final TenantExtractor tenantExtractor,
        final CasConfigurationProperties casProperties) {
        val adaptive = casProperties.getAuthn().getAdaptive();
        val intel = adaptive.getIpIntel();

        if (StringUtils.isNotBlank(intel.getRest().getUrl())) {
            return new RestfulIPAddressIntelligenceService(tenantExtractor, adaptive);
        }
        if (intel.getGroovy().getLocation() != null && CasRuntimeHintsRegistrar.notInNativeImage()) {
            return new GroovyIPAddressIntelligenceService(tenantExtractor, adaptive);
        }
        if (StringUtils.isNotBlank(intel.getBlackDot().getEmailAddress())) {
            return new BlackDotIPAddressIntelligenceService(tenantExtractor, adaptive);
        }
        return new DefaultIPAddressIntelligenceService(tenantExtractor, adaptive);
    }

    @Configuration(value = "CasCoreAuthenticationPolicyPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreAuthenticationPolicyPlanConfiguration {
        @ConditionalOnMissingBean(name = "authenticationPolicyExecutionPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationEventExecutionPlanConfigurer authenticationPolicyExecutionPlanConfigurer(
            final CasConfigurationProperties casProperties) {
            return plan -> {
                val policyProps = casProperties.getAuthn().getPolicy();
                val authPolicy = CoreAuthenticationUtils.newAuthenticationPolicy(policyProps);
                if (authPolicy != null) {
                    LOGGER.debug("Activated authentication policies are [{}]", authPolicy);
                    plan.registerAuthenticationPolicies(authPolicy);
                }
            };
        }

        @ConditionalOnMissingBean(name = AdaptiveAuthenticationPolicy.BEAN_NAME)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy(
            final CasConfigurationProperties casProperties,
            @Qualifier(IPAddressIntelligenceService.BEAN_NAME) final IPAddressIntelligenceService ipAddressIntelligenceService,
            @Qualifier(GeoLocationService.BEAN_NAME) final ObjectProvider<GeoLocationService> geoLocationService) {
            return new DefaultAdaptiveAuthenticationPolicy(geoLocationService.getIfAvailable(),
                ipAddressIntelligenceService, casProperties.getAuthn().getAdaptive());
        }
    }
}
