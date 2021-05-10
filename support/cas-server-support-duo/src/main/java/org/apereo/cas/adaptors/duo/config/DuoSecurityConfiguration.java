package org.apereo.cas.adaptors.duo.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.duo.config.cond.ConditionalOnDuoSecurityConfigured;
import org.apereo.cas.adaptors.duo.web.flow.DuoSecurityAuthenticationWebflowEventResolver;
import org.apereo.cas.adaptors.duo.web.flow.action.DuoSecurityAuthenticationWebflowAction;
import org.apereo.cas.adaptors.duo.web.flow.action.DuoSecurityDirectAuthenticationAction;
import org.apereo.cas.adaptors.duo.web.flow.action.DuoSecurityUniversalPromptPrepareLoginAction;
import org.apereo.cas.adaptors.duo.web.flow.action.DuoSecurityUniversalPromptValidateLoginAction;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderBean;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link DuoSecurityConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("duoSecurityConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnDuoSecurityConfigured
public class DuoSecurityConfiguration {
    @Autowired
    @Qualifier("duoProviderBean")
    private ObjectProvider<MultifactorAuthenticationProviderBean<
        DuoSecurityMultifactorAuthenticationProvider,
        DuoSecurityMultifactorAuthenticationProperties>> duoProviderBean;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private ObjectProvider<AuthenticationSystemSupport> authenticationSystemSupport;

    @Autowired
    @Qualifier("defaultTicketFactory")
    private ObjectProvider<TicketFactory> ticketFactory;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private ObjectProvider<CentralAuthenticationService> centralAuthenticationService;

    @Autowired
    @Qualifier("ticketRegistry")
    private ObjectProvider<TicketRegistry> ticketRegistry;

    @Autowired
    @Qualifier("casWebflowConfigurationContext")
    private ObjectProvider<CasWebflowEventResolutionConfigurationContext> casWebflowConfigurationContext;

    @ConditionalOnMissingBean(name = "duoNonWebAuthenticationAction")
    @Bean
    public Action duoNonWebAuthenticationAction() {
        return new DuoSecurityDirectAuthenticationAction();
    }

    @ConditionalOnMissingBean(name = "duoAuthenticationWebflowAction")
    @Bean
    @RefreshScope
    public Action duoAuthenticationWebflowAction() {
        return new DuoSecurityAuthenticationWebflowAction(duoAuthenticationWebflowEventResolver());
    }

    @ConditionalOnMissingBean(name = "duoUniversalPromptPrepareLoginAction")
    @Bean
    @RefreshScope
    public Action duoUniversalPromptPrepareLoginAction() {
        return new DuoSecurityUniversalPromptPrepareLoginAction(ticketRegistry.getObject(),
            duoProviderBean.getObject(), ticketFactory.getObject());
    }

    @ConditionalOnMissingBean(name = "duoUniversalPromptValidateLoginAction")
    @Bean
    @RefreshScope
    public Action duoUniversalPromptValidateLoginAction() {
        return new DuoSecurityUniversalPromptValidateLoginAction(
            duoAuthenticationWebflowEventResolver(),
            centralAuthenticationService.getObject(),
            duoProviderBean.getObject(),
            authenticationSystemSupport.getObject());
    }

    @ConditionalOnMissingBean(name = "duoAuthenticationWebflowEventResolver")
    @Bean
    public CasWebflowEventResolver duoAuthenticationWebflowEventResolver() {
        return new DuoSecurityAuthenticationWebflowEventResolver(casWebflowConfigurationContext.getObject());
    }
}
