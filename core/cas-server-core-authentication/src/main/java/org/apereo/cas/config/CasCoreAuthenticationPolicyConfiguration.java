package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationPolicy;
import org.apereo.cas.authentication.ContextualAuthenticationPolicyFactory;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.adaptive.DefaultAdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.authentication.policy.AllAuthenticationPolicy;
import org.apereo.cas.authentication.policy.AnyAuthenticationPolicy;
import org.apereo.cas.authentication.policy.NotPreventedAuthenticationPolicy;
import org.apereo.cas.authentication.policy.RequiredHandlerAuthenticationPolicy;
import org.apereo.cas.authentication.policy.RequiredHandlerAuthenticationPolicyFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.authentication.AuthenticationPolicyProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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

    @Autowired(required = false)
    @Qualifier("geoLocationService")
    private GeoLocationService geoLocationService;

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "authenticationPolicy")
    @Bean
    public AuthenticationPolicy authenticationPolicy() {
        final AuthenticationPolicyProperties police = casProperties.getAuthn().getPolicy();
        if (police.getReq().isEnabled()) {
            return new RequiredHandlerAuthenticationPolicy(police.getReq().getHandlerName(), police.getReq().isTryAll());
        }

        if (police.getAll().isEnabled()) {
            return new AllAuthenticationPolicy();
        }

        if (police.getNotPrevented().isEnabled()) {
            return new NotPreventedAuthenticationPolicy();
        }

        return new AnyAuthenticationPolicy(police.getAny().isTryAll());
    }

    @Bean
    public AuthenticationPolicy notPreventedAuthenticationPolicy() {
        return new NotPreventedAuthenticationPolicy();
    }

    @ConditionalOnMissingBean(name = "adaptiveAuthenticationPolicy")
    @Bean
    public AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy() {
        final DefaultAdaptiveAuthenticationPolicy p = new DefaultAdaptiveAuthenticationPolicy();
        p.setGeoLocationService(this.geoLocationService);
        p.setAdaptiveAuthenticationProperties(casProperties.getAuthn().getAdaptive());
        return p;
    }

    @Bean
    public ContextualAuthenticationPolicyFactory requiredHandlerAuthenticationPolicyFactory() {
        return new RequiredHandlerAuthenticationPolicyFactory();
    }

}
