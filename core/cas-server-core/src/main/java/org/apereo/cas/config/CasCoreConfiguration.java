package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.DefaultCentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategyConfigurer;
import org.apereo.cas.authentication.ContextualAuthenticationPolicyFactory;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.policy.AcceptAnyAuthenticationPolicyFactory;
import org.apereo.cas.authentication.policy.RequiredHandlerAuthenticationPolicyFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServiceContext;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.apache.commons.lang3.RegExUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.List;

/**
 * This is {@link CasCoreConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = true)
@Slf4j
public class CasCoreConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("casCommonComponents")
    private ObjectProvider<CasCommonComponents> casCommonComponents;

    @Bean
    @ConditionalOnMissingBean(name = "authenticationPolicyFactory")
    public ContextualAuthenticationPolicyFactory<ServiceContext> authenticationPolicyFactory() {
        if (casProperties.getAuthn().getPolicy().isRequiredHandlerAuthenticationPolicyEnabled()) {
            LOGGER.debug("Applying configuration for Required Handler Authentication Policy");
            return new RequiredHandlerAuthenticationPolicyFactory();
        }
        LOGGER.debug("Applying configuration for Accept Any Authentication Policy");
        return new AcceptAnyAuthenticationPolicyFactory();
    }

    @ConditionalOnMissingBean(name = "authenticationServiceSelectionPlan")
    @Autowired
    @Bean
    public AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan(final List<AuthenticationServiceSelectionStrategyConfigurer> configurers) {
        val plan = new DefaultAuthenticationServiceSelectionPlan();
        configurers.forEach(c -> {
            val name = RegExUtils.removePattern(c.getClass().getSimpleName(), "\\$.+");
            LOGGER.trace("Configuring authentication request service selection strategy plan [{}]", name);
            c.configureAuthenticationServiceSelectionStrategy(plan);
        });
        return plan;
    }

    @Bean
    @Autowired
    @ConditionalOnMissingBean(name = "centralAuthenticationService")
    public CentralAuthenticationService centralAuthenticationService(
            @Qualifier("authenticationServiceSelectionPlan") final AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan) {

        return new DefaultCentralAuthenticationService(
                casCommonComponents.getIfAvailable(),
                authenticationServiceSelectionPlan,
                authenticationPolicyFactory());
    }
}
