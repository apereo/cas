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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link DuoSecurityConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "duoSecurityConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnDuoSecurityConfigured
public class DuoSecurityConfiguration {

    @ConditionalOnMissingBean(name = "duoNonWebAuthenticationAction")
    @Bean
    public Action duoNonWebAuthenticationAction() {
        return new DuoSecurityDirectAuthenticationAction();
    }

    @ConditionalOnMissingBean(name = "duoAuthenticationWebflowAction")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public Action duoAuthenticationWebflowAction(
        @Qualifier("duoAuthenticationWebflowEventResolver")
        final CasWebflowEventResolver duoAuthenticationWebflowEventResolver) {
        return new DuoSecurityAuthenticationWebflowAction(duoAuthenticationWebflowEventResolver);
    }

    @ConditionalOnMissingBean(name = "duoUniversalPromptPrepareLoginAction")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public Action duoUniversalPromptPrepareLoginAction(
        @Qualifier("duoProviderBean")
        final MultifactorAuthenticationProviderBean<DuoSecurityMultifactorAuthenticationProvider, DuoSecurityMultifactorAuthenticationProperties> duoProviderBean,
        @Qualifier("defaultTicketFactory")
        final TicketFactory ticketFactory,
        @Qualifier(TicketRegistry.BEAN_NAME)
        final TicketRegistry ticketRegistry) {
        return new DuoSecurityUniversalPromptPrepareLoginAction(ticketRegistry, duoProviderBean, ticketFactory);
    }

    @ConditionalOnMissingBean(name = "duoUniversalPromptValidateLoginAction")
    @Bean
    @Autowired
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Action duoUniversalPromptValidateLoginAction(
        @Qualifier("duoAuthenticationWebflowEventResolver")
        final CasWebflowEventResolver duoAuthenticationWebflowEventResolver,
        @Qualifier(CentralAuthenticationService.BEAN_NAME)
        final CentralAuthenticationService centralAuthenticationService,
        @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
        final AuthenticationSystemSupport authenticationSystemSupport,
        @Qualifier("duoProviderBean")
        final MultifactorAuthenticationProviderBean<DuoSecurityMultifactorAuthenticationProvider, DuoSecurityMultifactorAuthenticationProperties> duoProviderBean) {
        return new DuoSecurityUniversalPromptValidateLoginAction(duoAuthenticationWebflowEventResolver, centralAuthenticationService,
            duoProviderBean, authenticationSystemSupport);
    }

    @ConditionalOnMissingBean(name = "duoAuthenticationWebflowEventResolver")
    @Bean
    @Autowired
    public CasWebflowEventResolver duoAuthenticationWebflowEventResolver(
        @Qualifier("casWebflowConfigurationContext")
        final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext) {
        return new DuoSecurityAuthenticationWebflowEventResolver(casWebflowConfigurationContext);
    }
}
