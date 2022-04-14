package org.apereo.cas.adaptors.duo.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityAuthenticationService;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.duo.web.flow.DuoSecurityAuthenticationWebflowEventResolver;
import org.apereo.cas.adaptors.duo.web.flow.action.DuoSecurityAuthenticationWebflowAction;
import org.apereo.cas.adaptors.duo.web.flow.action.DuoSecurityDirectAuthenticationAction;
import org.apereo.cas.adaptors.duo.web.flow.action.DuoSecurityMultifactorAuthenticationDeviceProviderAction;
import org.apereo.cas.adaptors.duo.web.flow.action.DuoSecurityUniversalPromptPrepareLoginAction;
import org.apereo.cas.adaptors.duo.web.flow.action.DuoSecurityUniversalPromptValidateLoginAction;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderBean;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.configuration.support.CasFeatureModule;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeature;
import org.apereo.cas.web.flow.actions.MultifactorAuthenticationDeviceProviderAction;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
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
@Configuration(value = "DuoSecurityConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeature(feature = CasFeatureModule.FeatureCatalog.MultifactorAuthentication, module = "duo")
public class DuoSecurityConfiguration {
    @ConditionalOnMissingBean(name = "duoNonWebAuthenticationAction")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Action duoNonWebAuthenticationAction(
        final ConfigurableApplicationContext applicationContext) {
        return BeanSupplier.of(Action.class)
            .when(DuoSecurityAuthenticationService.CONDITION.given(applicationContext.getEnvironment()))
            .supply(DuoSecurityDirectAuthenticationAction::new)
            .otherwiseProxy()
            .get();
    }

    @ConditionalOnMissingBean(name = "duoAuthenticationWebflowAction")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Action duoAuthenticationWebflowAction(
        @Qualifier("duoAuthenticationWebflowEventResolver")
        final CasWebflowEventResolver duoAuthenticationWebflowEventResolver) {
        return new DuoSecurityAuthenticationWebflowAction(duoAuthenticationWebflowEventResolver);
    }

    @ConditionalOnMissingBean(name = "duoMultifactorAuthenticationDeviceProviderAction")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MultifactorAuthenticationDeviceProviderAction duoMultifactorAuthenticationDeviceProviderAction(
        final ConfigurableApplicationContext applicationContext) {
        return new DuoSecurityMultifactorAuthenticationDeviceProviderAction(applicationContext);
    }
    @ConditionalOnMissingBean(name = "duoUniversalPromptPrepareLoginAction")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Action duoUniversalPromptPrepareLoginAction(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("duoProviderBean")
        final MultifactorAuthenticationProviderBean<DuoSecurityMultifactorAuthenticationProvider, DuoSecurityMultifactorAuthenticationProperties> duoProviderBean,
        @Qualifier(TicketFactory.BEAN_NAME)
        final TicketFactory ticketFactory,
        @Qualifier(TicketRegistry.BEAN_NAME)
        final TicketRegistry ticketRegistry) {
        return BeanSupplier.of(Action.class)
            .when(DuoSecurityAuthenticationService.CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> new DuoSecurityUniversalPromptPrepareLoginAction(ticketRegistry, duoProviderBean, ticketFactory))
            .otherwiseProxy()
            .get();
    }

    @ConditionalOnMissingBean(name = "duoUniversalPromptValidateLoginAction")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Action duoUniversalPromptValidateLoginAction(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("duoAuthenticationWebflowEventResolver")
        final CasWebflowEventResolver duoAuthenticationWebflowEventResolver,
        @Qualifier(CentralAuthenticationService.BEAN_NAME)
        final CentralAuthenticationService centralAuthenticationService,
        @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
        final AuthenticationSystemSupport authenticationSystemSupport,
        @Qualifier("duoProviderBean")
        final MultifactorAuthenticationProviderBean<DuoSecurityMultifactorAuthenticationProvider, DuoSecurityMultifactorAuthenticationProperties> duoProviderBean) {
        return BeanSupplier.of(Action.class)
            .when(DuoSecurityAuthenticationService.CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> new DuoSecurityUniversalPromptValidateLoginAction(
                duoAuthenticationWebflowEventResolver, centralAuthenticationService,
                duoProviderBean, authenticationSystemSupport))
            .otherwiseProxy()
            .get();
    }

    @ConditionalOnMissingBean(name = "duoAuthenticationWebflowEventResolver")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasWebflowEventResolver duoAuthenticationWebflowEventResolver(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("casWebflowConfigurationContext")
        final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext) {
        return BeanSupplier.of(CasWebflowEventResolver.class)
            .when(DuoSecurityAuthenticationService.CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> new DuoSecurityAuthenticationWebflowEventResolver(casWebflowConfigurationContext))
            .otherwiseProxy()
            .get();
    }
}
