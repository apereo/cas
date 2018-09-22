package org.apereo.cas.adaptors.duo.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.adaptors.duo.authn.DuoMfaProviderFactory;
import org.apereo.cas.adaptors.duo.authn.DuoMfaProviderFactoryBean;
import org.apereo.cas.adaptors.duo.web.flow.DuoAuthenticationWebflowEventResolver;
import org.apereo.cas.adaptors.duo.web.flow.action.DuoAuthenticationWebflowAction;
import org.apereo.cas.adaptors.duo.web.flow.action.DuoDirectAuthenticationAction;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.flow.authentication.RankedMultifactorAuthenticationProviderSelector;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.context.scope.GenericScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link DuoSecurityConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("duoSecurityConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class DuoSecurityConfiguration {

    @Autowired
    private GenericApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    private ObjectProvider<AuthenticationServiceSelectionPlan> authenticationRequestServiceSelectionStrategies;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private ObjectProvider<CentralAuthenticationService> centralAuthenticationService;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private ObjectProvider<AuthenticationSystemSupport> authenticationSystemSupport;

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private ObjectProvider<TicketRegistrySupport> ticketRegistrySupport;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("multifactorAuthenticationProviderSelector")
    private ObjectProvider<MultifactorAuthenticationProviderSelector> multifactorAuthenticationProviderSelector;

    @Autowired
    @Qualifier("warnCookieGenerator")
    private ObjectProvider<CookieGenerator> warnCookieGenerator;

    @ConditionalOnMissingBean(name = "duoNonWebAuthenticationAction")
    @Bean
    public Action duoNonWebAuthenticationAction() {
        return new DuoDirectAuthenticationAction();
    }

    @ConditionalOnMissingBean(name = "duoAuthenticationWebflowAction")
    @Bean
    public Action duoAuthenticationWebflowAction() {
        return new DuoAuthenticationWebflowAction(duoAuthenticationWebflowEventResolver());
    }

    @ConditionalOnMissingBean(name = "duoAuthenticationWebflowEventResolver")
    @Bean
    public CasWebflowEventResolver duoAuthenticationWebflowEventResolver() {
        return new DuoAuthenticationWebflowEventResolver(
            authenticationSystemSupport.getIfAvailable(),
            centralAuthenticationService.getIfAvailable(),
            servicesManager.getIfAvailable(),
            ticketRegistrySupport.getIfAvailable(),
            warnCookieGenerator.getIfAvailable(),
            authenticationRequestServiceSelectionStrategies.getIfAvailable(),
            multifactorAuthenticationProviderSelector.getIfAvailable(RankedMultifactorAuthenticationProviderSelector::new));
    }


    @Bean
    @RefreshScope
    public DuoMfaProviderFactoryBean duoMfaProviderFactoryBean() {
        return new DuoMfaProviderFactoryBean();
    }

    @Bean
    public DuoMfaProviderFactory duoMfaProviderFactory() {
        return new DuoMfaProviderFactory(applicationContext, casProperties.getAuthn().getMfa().getDuo());
    }
}
