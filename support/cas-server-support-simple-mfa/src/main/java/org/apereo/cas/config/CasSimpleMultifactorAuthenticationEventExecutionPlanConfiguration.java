package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.MultifactorAuthenticationFailureModeEvaluator;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.bypass.MultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.handler.ByCredentialTypeAuthenticationHandlerResolver;
import org.apereo.cas.authentication.metadata.AuthenticationContextAttributeMetaDataPopulator;
import org.apereo.cas.authentication.metadata.MultifactorAuthenticationProviderMetadataPopulator;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorAuthenticationHandler;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorAuthenticationProvider;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorTokenCredential;
import org.apereo.cas.mfa.simple.validation.CasSimpleMultifactorAuthenticationAccountService;
import org.apereo.cas.mfa.simple.validation.CasSimpleMultifactorAuthenticationService;
import org.apereo.cas.mfa.simple.validation.DefaultCasSimpleMultifactorAuthenticationService;
import org.apereo.cas.mfa.simple.validation.RestfulCasSimpleMultifactorAuthenticationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasSimpleMultifactorAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SimpleMFA)
@Configuration(value = "CasSimpleMultifactorAuthenticationEventExecutionPlanConfiguration", proxyBeanMethods = false)
class CasSimpleMultifactorAuthenticationEventExecutionPlanConfiguration {

    @ConditionalOnMissingBean(name = CasSimpleMultifactorAuthenticationService.BEAN_NAME)
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasSimpleMultifactorAuthenticationService casSimpleMultifactorAuthenticationService(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier(TicketFactory.BEAN_NAME)
        final TicketFactory ticketFactory,
        @Qualifier(CasSimpleMultifactorAuthenticationAccountService.BEAN_NAME)
        final ObjectProvider<CasSimpleMultifactorAuthenticationAccountService> accountService,
        @Qualifier(TicketRegistry.BEAN_NAME)
        final TicketRegistry ticketRegistry) {
        val simple = casProperties.getAuthn().getMfa().getSimple();
        return BeanSupplier.of(CasSimpleMultifactorAuthenticationService.class)
            .when(BeanCondition.on("cas.authn.mfa.simple.token.rest.url").isUrl().given(applicationContext.getEnvironment()))
            .supply(() -> new RestfulCasSimpleMultifactorAuthenticationService(ticketRegistry, simple.getToken().getRest(), ticketFactory))
            .otherwise(() -> new DefaultCasSimpleMultifactorAuthenticationService(ticketRegistry, ticketFactory, accountService))
            .get();
    }

    @ConditionalOnMissingBean(name = "casSimpleMultifactorAuthenticationHandler")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AuthenticationHandler casSimpleMultifactorAuthenticationHandler(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("casSimpleMultifactorAuthenticationProvider")
        final ObjectProvider<MultifactorAuthenticationProvider> multifactorAuthenticationProvider,
        @Qualifier("casSimpleMultifactorPrincipalFactory")
        final PrincipalFactory casSimpleMultifactorPrincipalFactory,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        @Qualifier(CasSimpleMultifactorAuthenticationService.BEAN_NAME)
        final CasSimpleMultifactorAuthenticationService casSimpleMultifactorAuthenticationService,
        final CasConfigurationProperties casProperties) {
        val props = casProperties.getAuthn().getMfa().getSimple();
        return new CasSimpleMultifactorAuthenticationHandler(props,
            applicationContext, casSimpleMultifactorPrincipalFactory,
            casSimpleMultifactorAuthenticationService, multifactorAuthenticationProvider);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "casSimpleMultifactorAuthenticationProvider")
    public MultifactorAuthenticationProvider casSimpleMultifactorAuthenticationProvider(
        @Qualifier("casSimpleMultifactorBypassEvaluator")
        final MultifactorAuthenticationProviderBypassEvaluator casSimpleMultifactorBypassEvaluator,
        @Qualifier("failureModeEvaluator")
        final MultifactorAuthenticationFailureModeEvaluator failureModeEvaluator,
        final CasConfigurationProperties casProperties) {
        val simple = casProperties.getAuthn().getMfa().getSimple();
        val p = new CasSimpleMultifactorAuthenticationProvider();
        p.setBypassEvaluator(casSimpleMultifactorBypassEvaluator);
        p.setFailureMode(simple.getFailureMode());
        p.setFailureModeEvaluator(failureModeEvaluator);
        p.setOrder(simple.getRank());
        p.setId(simple.getId());
        return p;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "casSimpleMultifactorAuthenticationMetaDataPopulator")
    public AuthenticationMetaDataPopulator casSimpleMultifactorAuthenticationMetaDataPopulator(
        @Qualifier("casSimpleMultifactorAuthenticationProvider")
        final MultifactorAuthenticationProvider casSimpleMultifactorAuthenticationProvider,
        @Qualifier("casSimpleMultifactorAuthenticationHandler")
        final AuthenticationHandler casSimpleMultifactorAuthenticationHandler,
        final CasConfigurationProperties casProperties) {
        return new AuthenticationContextAttributeMetaDataPopulator(
            casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute(),
            casSimpleMultifactorAuthenticationHandler,
            casSimpleMultifactorAuthenticationProvider.getId()
        );
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "casSimpleMultifactorProviderAuthenticationMetadataPopulator")
    public AuthenticationMetaDataPopulator casSimpleMultifactorProviderAuthenticationMetadataPopulator(
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        final CasConfigurationProperties casProperties,
        @Qualifier("casSimpleMultifactorAuthenticationProvider")
        final ObjectProvider<MultifactorAuthenticationProvider> multifactorAuthenticationProvider) {
        val authenticationContextAttribute = casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute();
        return new MultifactorAuthenticationProviderMetadataPopulator(authenticationContextAttribute,
            multifactorAuthenticationProvider, servicesManager);
    }

    @ConditionalOnMissingBean(name = "casSimpleMultifactorPrincipalFactory")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PrincipalFactory casSimpleMultifactorPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "casSimpleMultifactorAuthenticationEventExecutionPlanConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AuthenticationEventExecutionPlanConfigurer casSimpleMultifactorAuthenticationEventExecutionPlanConfigurer(
        @Qualifier("casSimpleMultifactorProviderAuthenticationMetadataPopulator")
        final AuthenticationMetaDataPopulator casSimpleMultifactorProviderAuthenticationMetadataPopulator,
        @Qualifier("casSimpleMultifactorAuthenticationHandler")
        final AuthenticationHandler casSimpleMultifactorAuthenticationHandler,
        @Qualifier("casSimpleMultifactorAuthenticationMetaDataPopulator")
        final AuthenticationMetaDataPopulator casSimpleMultifactorAuthenticationMetaDataPopulator,
        @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
        final PrincipalResolver principalResolver) {
        return plan -> {
            plan.registerAuthenticationHandler(casSimpleMultifactorAuthenticationHandler);
            plan.registerAuthenticationMetadataPopulator(casSimpleMultifactorAuthenticationMetaDataPopulator);
            plan.registerAuthenticationMetadataPopulator(casSimpleMultifactorProviderAuthenticationMetadataPopulator);
            plan.registerAuthenticationHandlerResolver(new ByCredentialTypeAuthenticationHandlerResolver(CasSimpleMultifactorTokenCredential.class));
        };
    }
}
