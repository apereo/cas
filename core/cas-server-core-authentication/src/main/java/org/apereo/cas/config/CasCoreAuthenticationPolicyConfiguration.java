package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.adaptive.DefaultAdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.authentication.adaptive.intel.IPAddressIntelligenceService;
import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasCoreAuthenticationPolicyConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration(value = "casCoreAuthenticationPolicyConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasCoreAuthenticationPolicyConfiguration {

    @ConditionalOnMissingBean(name = "authenticationPolicyExecutionPlanConfigurer")
    @Bean
    @RefreshScope
    @Autowired
    public AuthenticationEventExecutionPlanConfigurer authenticationPolicyExecutionPlanConfigurer(
        final CasConfigurationProperties casProperties) {
        return plan -> {
            val policyProps = casProperties.getAuthn().getPolicy();
            val authPolicy = CoreAuthenticationUtils.newAuthenticationPolicy(policyProps);
            if (authPolicy != null) {
                plan.registerAuthenticationPolicies(authPolicy);
            }
        };
    }

    @ConditionalOnMissingBean(name = "adaptiveAuthenticationPolicy")
    @Bean
    @RefreshScope
    @Autowired
    public AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy(
        final CasConfigurationProperties casProperties,
        @Qualifier("ipAddressIntelligenceService")
        final IPAddressIntelligenceService ipAddressIntelligenceService,
        @Qualifier("geoLocationService")
        final ObjectProvider<GeoLocationService> geoLocationService) {
        return new DefaultAdaptiveAuthenticationPolicy(geoLocationService.getIfAvailable(),
            ipAddressIntelligenceService, casProperties.getAuthn().getAdaptive());
    }

    @ConditionalOnMissingBean(name = "ipAddressIntelligenceService")
    @Bean
    @RefreshScope
    @Autowired
    public IPAddressIntelligenceService ipAddressIntelligenceService(final CasConfigurationProperties casProperties) {
        val adaptive = casProperties.getAuthn().getAdaptive();
        return CoreAuthenticationUtils.newIpAddressIntelligenceService(adaptive);
    }
}
