package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationPolicy;
import org.apereo.cas.authentication.ContextualAuthenticationPolicyFactory;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.adaptive.DefaultAdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.authentication.policy.AllAuthenticationPolicy;
import org.apereo.cas.authentication.policy.AnyAuthenticationPolicy;
import org.apereo.cas.authentication.policy.GroovyScriptAuthenticationPolicy;
import org.apereo.cas.authentication.policy.NotPreventedAuthenticationPolicy;
import org.apereo.cas.authentication.policy.RequiredHandlerAuthenticationPolicy;
import org.apereo.cas.authentication.policy.RequiredHandlerAuthenticationPolicyFactory;
import org.apereo.cas.authentication.policy.RestfulAuthenticationPolicy;
import org.apereo.cas.authentication.policy.UniquePrincipalAuthenticationPolicy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.authentication.AuthenticationPolicyProperties;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
    private ApplicationContext applicationContext;
    
    @Autowired(required = false)
    @Qualifier("geoLocationService")
    private GeoLocationService geoLocationService;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "authenticationPolicy")
    @Bean
    public Collection<AuthenticationPolicy> authenticationPolicy() {
        final AuthenticationPolicyProperties police = casProperties.getAuthn().getPolicy();
        final List<AuthenticationPolicy> policies = new ArrayList<>();

        if (police.getReq().isEnabled()) {
            policies.add(new RequiredHandlerAuthenticationPolicy(police.getReq().getHandlerName(), police.getReq().isTryAll()));
            return policies;
        }

        if (police.getAll().isEnabled()) {
            policies.add(new AllAuthenticationPolicy());
            return policies;
        }

        if (police.getNotPrevented().isEnabled()) {
            policies.add(new NotPreventedAuthenticationPolicy());
            return policies;
        }

        if (police.getUniquePrincipal().isEnabled()) {
            /*
             * This is explicitly retrieved from the application context
             * in order to avoid circular and leaking dependencies.
             */
            final TicketRegistry ticketRegistry = this.applicationContext.getBean("ticketRegistry", TicketRegistry.class);
            policies.add(new UniquePrincipalAuthenticationPolicy(ticketRegistry));
            return policies;
        }
        
        if (!police.getGroovy().isEmpty()) {
            police.getGroovy().forEach(groovy -> policies.add(new GroovyScriptAuthenticationPolicy(resourceLoader, groovy.getScript())));
            return policies;
        }

        if (!police.getRest().isEmpty()) {
            police.getRest().forEach(r -> policies.add(new RestfulAuthenticationPolicy(new RestTemplate(), r.getEndpoint())));
            return policies;
        }

        policies.add(new AnyAuthenticationPolicy(police.getAny().isTryAll()));
        return policies;
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

    @ConditionalOnMissingBean(name = "requiredHandlerAuthenticationPolicyFactory")
    @Bean
    public ContextualAuthenticationPolicyFactory requiredHandlerAuthenticationPolicyFactory() {
        return new RequiredHandlerAuthenticationPolicyFactory();
    }

}
