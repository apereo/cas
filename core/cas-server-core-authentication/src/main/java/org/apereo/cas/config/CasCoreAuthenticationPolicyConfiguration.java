package org.apereo.cas.config;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
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
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.client.RestTemplate;

/**
 * This is {@link CasCoreAuthenticationPolicyConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("casCoreAuthenticationPolicyConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasCoreAuthenticationPolicyConfiguration {

    @Autowired
    @Qualifier("ticketRegistry")
    private ObjectProvider<TicketRegistry> ticketRegistry;

    @Autowired
    @Qualifier("geoLocationService")
    private ObjectProvider<GeoLocationService> geoLocationService;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "authenticationPolicyExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer authenticationPolicyExecutionPlanConfigurer() {
        return plan -> {
            final var police = casProperties.getAuthn().getPolicy();

            if (police.getReq().isEnabled()) {
                LOGGER.debug("Activating authentication policy [{}]", RequiredHandlerAuthenticationPolicy.class.getSimpleName());
                plan.registerAuthenticationPolicy(new RequiredHandlerAuthenticationPolicy(police.getReq().getHandlerName(), police.getReq().isTryAll()));
            } else if (police.getAll().isEnabled()) {
                LOGGER.debug("Activating authentication policy [{}]", AllAuthenticationPolicy.class.getSimpleName());
                plan.registerAuthenticationPolicy(new AllAuthenticationPolicy());
            } else if (police.getNotPrevented().isEnabled()) {
                LOGGER.debug("Activating authentication policy [{}]", NotPreventedAuthenticationPolicy.class.getSimpleName());
                plan.registerAuthenticationPolicy(notPreventedAuthenticationPolicy());
            } else if (police.getUniquePrincipal().isEnabled()) {
                LOGGER.debug("Activating authentication policy [{}]", UniquePrincipalAuthenticationPolicy.class.getSimpleName());
                plan.registerAuthenticationPolicy(new UniquePrincipalAuthenticationPolicy(ticketRegistry.getIfAvailable()));
            } else if (!police.getGroovy().isEmpty()) {
                LOGGER.debug("Activating authentication policy [{}]", GroovyScriptAuthenticationPolicy.class.getSimpleName());
                police.getGroovy().forEach(groovy -> plan.registerAuthenticationPolicy(new GroovyScriptAuthenticationPolicy(resourceLoader, groovy.getScript())));
            } else if (!police.getRest().isEmpty()) {
                LOGGER.debug("Activating authentication policy [{}]", RestfulAuthenticationPolicy.class.getSimpleName());
                police.getRest().forEach(r -> plan.registerAuthenticationPolicy(new RestfulAuthenticationPolicy(new RestTemplate(), r.getEndpoint())));
            } else if (police.getAny().isEnabled()) {
                LOGGER.debug("Activating authentication policy [{}]", AnyAuthenticationPolicy.class.getSimpleName());
                plan.registerAuthenticationPolicy(new AnyAuthenticationPolicy(police.getAny().isTryAll()));
            }
        };
    }

    @Bean
    public AuthenticationPolicy notPreventedAuthenticationPolicy() {
        return new NotPreventedAuthenticationPolicy();
    }

    @ConditionalOnMissingBean(name = "adaptiveAuthenticationPolicy")
    @Bean
    @RefreshScope
    public AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy() {
        return new DefaultAdaptiveAuthenticationPolicy(this.geoLocationService.getIfAvailable(), casProperties.getAuthn().getAdaptive());
    }

    @ConditionalOnMissingBean(name = "requiredHandlerAuthenticationPolicyFactory")
    @Bean
    public ContextualAuthenticationPolicyFactory requiredHandlerAuthenticationPolicyFactory() {
        return new RequiredHandlerAuthenticationPolicyFactory();
    }

}
