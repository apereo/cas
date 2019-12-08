package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationPolicy;
import org.apereo.cas.authentication.ContextualAuthenticationPolicyFactory;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.adaptive.DefaultAdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.authentication.adaptive.intel.GroovyIPAddressIntelligenceService;
import org.apereo.cas.authentication.adaptive.intel.IPAddressIntelligenceService;
import org.apereo.cas.authentication.adaptive.intel.RestfulIPAddressIntelligenceService;
import org.apereo.cas.authentication.policy.AllAuthenticationHandlersSucceededAuthenticationPolicy;
import org.apereo.cas.authentication.policy.AllCredentialsValidatedAuthenticationPolicy;
import org.apereo.cas.authentication.policy.AtLeastOneCredentialValidatedAuthenticationPolicy;
import org.apereo.cas.authentication.policy.GroovyScriptAuthenticationPolicy;
import org.apereo.cas.authentication.policy.NotPreventedAuthenticationPolicy;
import org.apereo.cas.authentication.policy.RequiredHandlerAuthenticationPolicy;
import org.apereo.cas.authentication.policy.RequiredHandlerAuthenticationPolicyFactory;
import org.apereo.cas.authentication.policy.RestfulAuthenticationPolicy;
import org.apereo.cas.authentication.policy.UniquePrincipalAuthenticationPolicy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.registry.TicketRegistry;

import lombok.extern.slf4j.Slf4j;
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
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "authenticationPolicyExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer authenticationPolicyExecutionPlanConfigurer() {
        return plan -> {
            val police = casProperties.getAuthn().getPolicy();

            if (police.getReq().isEnabled()) {
                LOGGER.trace("Activating authentication policy [{}]", RequiredHandlerAuthenticationPolicy.class.getSimpleName());
                plan.registerAuthenticationPolicy(new RequiredHandlerAuthenticationPolicy(police.getReq().getHandlerName(), police.getReq().isTryAll()));
            } else if (police.getAllHandlers().isEnabled()) {
                LOGGER.trace("Activating authentication policy [{}]", AllAuthenticationHandlersSucceededAuthenticationPolicy.class.getSimpleName());
                plan.registerAuthenticationPolicy(new AllAuthenticationHandlersSucceededAuthenticationPolicy());
            } else if (police.getAll().isEnabled()) {
                LOGGER.trace("Activating authentication policy [{}]", AllCredentialsValidatedAuthenticationPolicy.class.getSimpleName());
                plan.registerAuthenticationPolicy(new AllCredentialsValidatedAuthenticationPolicy());
            } else if (police.getNotPrevented().isEnabled()) {
                LOGGER.trace("Activating authentication policy [{}]", NotPreventedAuthenticationPolicy.class.getSimpleName());
                plan.registerAuthenticationPolicy(notPreventedAuthenticationPolicy());
            } else if (police.getUniquePrincipal().isEnabled()) {
                LOGGER.trace("Activating authentication policy [{}]", UniquePrincipalAuthenticationPolicy.class.getSimpleName());
                plan.registerAuthenticationPolicy(new UniquePrincipalAuthenticationPolicy(ticketRegistry.getObject()));
            } else if (!police.getGroovy().isEmpty()) {
                LOGGER.trace("Activating authentication policy [{}]", GroovyScriptAuthenticationPolicy.class.getSimpleName());
                police.getGroovy().forEach(groovy -> plan.registerAuthenticationPolicy(new GroovyScriptAuthenticationPolicy(groovy.getScript())));
            } else if (!police.getRest().isEmpty()) {
                LOGGER.trace("Activating authentication policy [{}]", RestfulAuthenticationPolicy.class.getSimpleName());
                police.getRest().forEach(r -> plan.registerAuthenticationPolicy(new RestfulAuthenticationPolicy(new RestTemplate(), r.getEndpoint())));
            } else if (police.getAny().isEnabled()) {
                LOGGER.trace("Activating authentication policy [{}]", AtLeastOneCredentialValidatedAuthenticationPolicy.class.getSimpleName());
                plan.registerAuthenticationPolicy(new AtLeastOneCredentialValidatedAuthenticationPolicy(police.getAny().isTryAll()));
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
        return IPAddressIntelligenceService.allowed();
    }
}
