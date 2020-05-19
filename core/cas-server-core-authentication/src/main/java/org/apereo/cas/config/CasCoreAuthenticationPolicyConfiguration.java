package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.ContextualAuthenticationPolicyFactory;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.adaptive.DefaultAdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.authentication.adaptive.intel.DefaultIPAddressIntelligenceService;
import org.apereo.cas.authentication.adaptive.intel.GroovyIPAddressIntelligenceService;
import org.apereo.cas.authentication.adaptive.intel.IPAddressIntelligenceService;
import org.apereo.cas.authentication.adaptive.intel.RestfulIPAddressIntelligenceService;
import org.apereo.cas.authentication.policy.RequiredHandlerAuthenticationPolicyFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
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
@Configuration("casCoreAuthenticationPolicyConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasCoreAuthenticationPolicyConfiguration {

    @Autowired
    @Qualifier("geoLocationService")
    private ObjectProvider<GeoLocationService> geoLocationService;

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "authenticationPolicyExecutionPlanConfigurer")
    @Bean
    @RefreshScope
    public AuthenticationEventExecutionPlanConfigurer authenticationPolicyExecutionPlanConfigurer() {
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
    public AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy() {
        return new DefaultAdaptiveAuthenticationPolicy(this.geoLocationService.getIfAvailable(),
            ipAddressIntelligenceService(), casProperties.getAuthn().getAdaptive());
    }

    @ConditionalOnMissingBean(name = "requiredHandlerAuthenticationPolicyFactory")
    @Bean
    public ContextualAuthenticationPolicyFactory requiredHandlerAuthenticationPolicyFactory() {
        return new RequiredHandlerAuthenticationPolicyFactory();
    }

    @ConditionalOnMissingBean(name = "ipAddressIntelligenceService")
    @Bean
    @RefreshScope
    public IPAddressIntelligenceService ipAddressIntelligenceService() {
        val adaptive = casProperties.getAuthn().getAdaptive();
        val intel = adaptive.getIpIntel();

        if (StringUtils.isNotBlank(intel.getRest().getUrl())) {
            return new RestfulIPAddressIntelligenceService(adaptive);
        }
        if (intel.getGroovy().getLocation() != null) {
            return new GroovyIPAddressIntelligenceService(adaptive);
        }
        if (StringUtils.isNotBlank(intel.getBlackDot().getEmailAddress())) {
            return new RestfulIPAddressIntelligenceService(adaptive);
        }
        return new DefaultIPAddressIntelligenceService(adaptive);
    }
}
