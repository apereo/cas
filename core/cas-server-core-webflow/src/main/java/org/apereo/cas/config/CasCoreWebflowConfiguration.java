package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationContextValidator;
import org.apereo.cas.authentication.MultifactorAuthenticationFailedException;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderAbsentException;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.authentication.MultifactorAuthenticationRequiredException;
import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.authentication.adaptive.UnauthorizedAuthenticationException;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.exceptions.InvalidLoginLocationException;
import org.apereo.cas.authentication.exceptions.InvalidLoginTimeException;
import org.apereo.cas.authentication.exceptions.UniquePrincipalRequiredException;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.ResponseBuilderLocator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceForPrincipalException;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.ticket.UnsatisfiedAuthenticationPolicyException;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.cipher.WebflowConversationStateCipherExecutor;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.util.spring.boot.ConditionalOnMissingGraalVMNativeImage;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowCredentialProvider;
import org.apereo.cas.web.flow.ChainingSingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.DefaultSingleSignOnBuildingStrategy;
import org.apereo.cas.web.flow.DefaultSingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.SingleSignOnBuildingStrategy;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategyConfigurer;
import org.apereo.cas.web.flow.actions.AuthenticationExceptionHandlerAction;
import org.apereo.cas.web.flow.actions.CheckWebAuthenticationRequestAction;
import org.apereo.cas.web.flow.actions.ClearWebflowCredentialAction;
import org.apereo.cas.web.flow.actions.InjectResponseHeadersAction;
import org.apereo.cas.web.flow.actions.RedirectToServiceAction;
import org.apereo.cas.web.flow.actions.RenewAuthenticationRequestCheckAction;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;
import org.apereo.cas.web.flow.actions.storage.PutBrowserStorageAction;
import org.apereo.cas.web.flow.actions.storage.ReadBrowserStorageAction;
import org.apereo.cas.web.flow.actions.storage.WriteBrowserStorageAction;
import org.apereo.cas.web.flow.authentication.CasWebflowExceptionCatalog;
import org.apereo.cas.web.flow.authentication.CasWebflowExceptionConfigurer;
import org.apereo.cas.web.flow.authentication.CasWebflowExceptionHandler;
import org.apereo.cas.web.flow.authentication.DefaultCasWebflowAbstractTicketExceptionHandler;
import org.apereo.cas.web.flow.authentication.DefaultCasWebflowAuthenticationExceptionHandler;
import org.apereo.cas.web.flow.authentication.DefaultCasWebflowCredentialProvider;
import org.apereo.cas.web.flow.authentication.DefaultCasWebflowExceptionCatalog;
import org.apereo.cas.web.flow.authentication.GenericCasWebflowExceptionHandler;
import org.apereo.cas.web.flow.authentication.GroovyCasWebflowAuthenticationExceptionHandler;
import org.apereo.cas.web.flow.authentication.RegisteredServiceAuthenticationPolicySingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.decorator.GroovyLoginWebflowDecorator;
import org.apereo.cas.web.flow.decorator.RestfulLoginWebflowDecorator;
import org.apereo.cas.web.flow.decorator.WebflowDecorator;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
import org.apereo.cas.web.flow.resolver.impl.ServiceTicketRequestWebflowEventResolver;
import org.apereo.cas.web.support.ArgumentExtractor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.webflow.execution.Action;
import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is {@link CasCoreWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Webflow)
@Configuration(value = "CasCoreWebflowConfiguration", proxyBeanMethods = false)
class CasCoreWebflowConfiguration {

    @Configuration(value = "CasCoreWebflowEventResolutionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreWebflowEventResolutionConfiguration {
        @ConditionalOnMissingBean(name = CasWebflowEventResolver.BEAN_NAME_SERVICE_TICKET_EVENT_RESOLVER)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowEventResolver serviceTicketRequestWebflowEventResolver(
            @Qualifier(CasWebflowEventResolutionConfigurationContext.BEAN_NAME)
            final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext) {
            return new ServiceTicketRequestWebflowEventResolver(casWebflowConfigurationContext);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "webflowCipherExecutor")
        public CipherExecutor webflowCipherExecutor(final CasConfigurationProperties casProperties) {
            val webflow = casProperties.getWebflow();
            val crypto = webflow.getCrypto();

            var enabled = crypto.isEnabled();
            if (!enabled && StringUtils.isNotBlank(crypto.getEncryption().getKey()) && StringUtils.isNotBlank(crypto.getSigning().getKey())) {
                LOGGER.warn("Webflow encryption/signing is not enabled explicitly in the configuration, yet signing/encryption keys "
                            + "are defined for operations. CAS will proceed to enable the webflow encryption/signing functionality.");
                enabled = true;
            }
            if (enabled) {
                return new WebflowConversationStateCipherExecutor(
                    crypto.getEncryption().getKey(),
                    crypto.getSigning().getKey(),
                    crypto.getAlg(),
                    crypto.getSigning().getKeySize(),
                    crypto.getEncryption().getKeySize());
            }
            LOGGER.warn("Webflow encryption/signing is turned off. This "
                        + "MAY NOT be safe in a production environment. Consider using other choices to handle encryption, "
                        + "signing and verification of webflow state.");
            return CipherExecutor.noOp();
        }
    }

    @Configuration(value = "CasCoreWebflowContextConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreWebflowContextConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowEventResolutionConfigurationContext.BEAN_NAME)
        public CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(PrincipalFactory.BEAN_NAME)
            final PrincipalFactory principalFactory,
            @Qualifier(TicketRegistrySupport.BEAN_NAME)
            final TicketRegistrySupport ticketRegistrySupport,
            @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
            final AuthenticationSystemSupport authenticationSystemSupport,
            @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
            final AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService,
            @Qualifier(MultifactorAuthenticationContextValidator.BEAN_NAME)
            final MultifactorAuthenticationContextValidator authenticationContextValidator,
            @Qualifier(AuthenticationEventExecutionPlan.DEFAULT_BEAN_NAME)
            final AuthenticationEventExecutionPlan authenticationEventExecutionPlan,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(CasCookieBuilder.BEAN_NAME_WARN_COOKIE_BUILDER)
            final CasCookieBuilder warnCookieGenerator,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier(SingleSignOnParticipationStrategy.BEAN_NAME)
            final SingleSignOnParticipationStrategy webflowSingleSignOnParticipationStrategy,
            @Qualifier(SingleSignOnBuildingStrategy.BEAN_NAME)
            final SingleSignOnBuildingStrategy singleSignOnBuildingStrategy,
            @Qualifier(AuditableExecution.AUDITABLE_EXECUTION_REGISTERED_SERVICE_ACCESS)
            final AuditableExecution registeredServiceAccessStrategyEnforcer,
            @Qualifier(CasCookieBuilder.BEAN_NAME_TICKET_GRANTING_COOKIE_BUILDER)
            final CasCookieBuilder ticketGrantingTicketCookieGenerator,
            @Qualifier(ArgumentExtractor.BEAN_NAME)
            final ArgumentExtractor argumentExtractor,
            @Qualifier(TenantExtractor.BEAN_NAME)
            final TenantExtractor tenantExtractor,
            @Qualifier(CasWebflowExceptionCatalog.BEAN_NAME)
            final CasWebflowExceptionCatalog casWebflowExceptionCatalog,
            @Qualifier(CasWebflowCredentialProvider.BEAN_NAME)
            final CasWebflowCredentialProvider casWebflowCredentialProvider,
            @Qualifier(MultifactorAuthenticationProviderSelector.BEAN_NAME)
            final MultifactorAuthenticationProviderSelector multifactorAuthenticationProviderSelector) {
            return CasWebflowEventResolutionConfigurationContext
                .builder()
                .casWebflowCredentialProvider(casWebflowCredentialProvider)
                .authenticationContextValidator(authenticationContextValidator)
                .authenticationSystemSupport(authenticationSystemSupport)
                .argumentExtractors(CollectionUtils.wrap(argumentExtractor))
                .centralAuthenticationService(centralAuthenticationService)
                .servicesManager(servicesManager)
                .ticketRegistrySupport(ticketRegistrySupport)
                .warnCookieGenerator(warnCookieGenerator)
                .authenticationRequestServiceSelectionStrategies(authenticationServiceSelectionPlan)
                .registeredServiceAccessStrategyEnforcer(registeredServiceAccessStrategyEnforcer)
                .casProperties(casProperties)
                .ticketRegistry(ticketRegistry)
                .singleSignOnParticipationStrategy(webflowSingleSignOnParticipationStrategy)
                .singleSignOnBuildingStrategy(singleSignOnBuildingStrategy)
                .applicationContext(applicationContext)
                .ticketGrantingTicketCookieGenerator(ticketGrantingTicketCookieGenerator)
                .authenticationEventExecutionPlan(authenticationEventExecutionPlan)
                .principalFactory(principalFactory)
                .multifactorAuthenticationProviderSelector(multifactorAuthenticationProviderSelector)
                .casWebflowExceptionCatalog(casWebflowExceptionCatalog)
                .tenantExtractor(tenantExtractor)
                .build();
        }
    }

    @Configuration(value = "CasCoreWebflowActionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreWebflowActionConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_CLEAR_WEBFLOW_CREDENTIALS)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action clearWebflowCredentialsAction(final ConfigurableApplicationContext applicationContext,
                                                    final CasConfigurationProperties casProperties) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(ClearWebflowCredentialAction::new)
                .withId(CasWebflowConstants.ACTION_ID_CLEAR_WEBFLOW_CREDENTIALS)
                .build()
                .get();
        }

        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_CHECK_WEB_AUTHENTICATION_REQUEST)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action checkWebAuthenticationRequestAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new CheckWebAuthenticationRequestAction(casProperties.getAuthn().getMfa().getCore().getContentType()))
                .withId(CasWebflowConstants.ACTION_ID_CHECK_WEB_AUTHENTICATION_REQUEST)
                .build()
                .get();
        }

        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_RENEW_AUTHN_REQUEST)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action renewAuthenticationRequestCheckAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(SingleSignOnParticipationStrategy.BEAN_NAME)
            final SingleSignOnParticipationStrategy singleSignOnParticipationStrategy) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new RenewAuthenticationRequestCheckAction(singleSignOnParticipationStrategy))
                .withId(CasWebflowConstants.ACTION_ID_RENEW_AUTHN_REQUEST)
                .build()
                .get();
        }
        

        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_REDIRECT_TO_SERVICE)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action redirectToServiceAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("webApplicationResponseBuilderLocator")
            final ResponseBuilderLocator responseBuilderLocator) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new RedirectToServiceAction(responseBuilderLocator))
                .withId(CasWebflowConstants.ACTION_ID_REDIRECT_TO_SERVICE)
                .build()
                .get();
        }

        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_INJECT_RESPONSE_HEADERS)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action injectResponseHeadersAction(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("webApplicationResponseBuilderLocator")
            final ResponseBuilderLocator responseBuilderLocator) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new InjectResponseHeadersAction(responseBuilderLocator))
                .withId(CasWebflowConstants.ACTION_ID_INJECT_RESPONSE_HEADERS)
                .build()
                .get();
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_AUTHENTICATION_EXCEPTION_HANDLER)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action authenticationExceptionHandler(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            final List<CasWebflowExceptionHandler> handlers) {
            AnnotationAwareOrderComparator.sort(handlers);
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new AuthenticationExceptionHandlerAction(handlers))
                .withId(CasWebflowConstants.ACTION_ID_AUTHENTICATION_EXCEPTION_HANDLER)
                .build()
                .get();
        }
    }

    @Configuration(value = "CasCoreWebflowExceptionHandlingConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreWebflowExceptionHandlingConfiguration {
        @ConditionalOnMissingBean(name = "groovyCasWebflowAuthenticationExceptionHandler")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingGraalVMNativeImage
        public CasWebflowExceptionHandler<Exception> groovyCasWebflowAuthenticationExceptionHandler(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) throws Exception {
            return BeanSupplier.of(CasWebflowExceptionHandler.class)
                .when(BeanCondition.on("cas.authn.errors.groovy.location").exists().given(applicationContext.getEnvironment()))
                .supply(() -> new GroovyCasWebflowAuthenticationExceptionHandler(
                    casProperties.getAuthn().getErrors().getGroovy().getLocation(), applicationContext))
                .otherwiseProxy()
                .get();
        }

        @ConditionalOnMissingBean(name = "defaultCasWebflowAuthenticationExceptionHandler")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowExceptionHandler<AuthenticationException> defaultCasWebflowAuthenticationExceptionHandler(
            @Qualifier(CasWebflowExceptionCatalog.BEAN_NAME)
            final CasWebflowExceptionCatalog handledAuthenticationExceptions) {
            return new DefaultCasWebflowAuthenticationExceptionHandler(handledAuthenticationExceptions);
        }

        @ConditionalOnMissingBean(name = "defaultCasWebflowAbstractTicketExceptionHandler")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowExceptionHandler<AbstractTicketException> defaultCasWebflowAbstractTicketExceptionHandler(
            @Qualifier(CasWebflowExceptionCatalog.BEAN_NAME)
            final CasWebflowExceptionCatalog handledAuthenticationExceptions) {
            return new DefaultCasWebflowAbstractTicketExceptionHandler(handledAuthenticationExceptions);
        }

        @ConditionalOnMissingBean(name = "genericCasWebflowExceptionHandler")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowExceptionHandler genericCasWebflowExceptionHandler(
            @Qualifier(CasWebflowExceptionCatalog.BEAN_NAME)
            final CasWebflowExceptionCatalog handledAuthenticationExceptions) {
            return new GenericCasWebflowExceptionHandler(handledAuthenticationExceptions);
        }
    }

    @Configuration(value = "CasCoreWebflowExceptionCatalogConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreWebflowExceptionCatalogConfiguration {

        /**
         * Handled authentication exceptions set.
         * Order is important here; We want the account policy exceptions to be handled
         * first before moving onto more generic errors. In the event that multiple handlers
         * are defined, where one fails due to account policy restriction and one fails
         * due to a bad password, we want the error associated with the account policy
         * to be processed first, rather than presenting a more generic error associated
         *
         * @param casProperties the cas properties
         * @return the set
         */
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = CasWebflowExceptionCatalog.BEAN_NAME)
        public CasWebflowExceptionCatalog casWebflowExceptionCatalog(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            val catalog = new DefaultCasWebflowExceptionCatalog();
            catalog.registerException(AccountLockedException.class);
            catalog.registerException(CredentialExpiredException.class);
            catalog.registerException(AccountExpiredException.class);
            catalog.registerException(AccountDisabledException.class);
            catalog.registerException(InvalidLoginLocationException.class);
            catalog.registerException(AccountPasswordMustChangeException.class);
            catalog.registerException(InvalidLoginTimeException.class);
            catalog.registerException(UniquePrincipalRequiredException.class);

            catalog.registerException(AccountNotFoundException.class);
            catalog.registerException(FailedLoginException.class);
            catalog.registerException(UnauthorizedServiceForPrincipalException.class);
            catalog.registerException(PrincipalException.class);
            catalog.registerException(UnsatisfiedAuthenticationPolicyException.class);
            catalog.registerException(UnauthorizedAuthenticationException.class);
            catalog.registerException(MultifactorAuthenticationProviderAbsentException.class);
            catalog.registerException(MultifactorAuthenticationRequiredException.class);
            catalog.registerException(MultifactorAuthenticationFailedException.class);
            catalog.registerExceptions(casProperties.getAuthn().getErrors().getExceptions());

            val configurers = applicationContext.getBeansOfType(CasWebflowExceptionConfigurer.class)
                .values()
                .stream()
                .filter(BeanSupplier::isNotProxy)
                .collect(Collectors.toList());
            AnnotationAwareOrderComparator.sort(configurers);
            configurers.forEach(cfg -> cfg.configure(catalog));

            return catalog;
        }
    }

    @Configuration(value = "CasCoreWebflowSingleSignOnConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreWebflowSingleSignOnConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = SingleSignOnParticipationStrategy.BEAN_NAME)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SingleSignOnParticipationStrategy singleSignOnParticipationStrategy(
            final List<SingleSignOnParticipationStrategyConfigurer> providers) {
            AnnotationAwareOrderComparator.sort(providers);
            val chain = new ChainingSingleSignOnParticipationStrategy();
            providers.forEach(provider -> provider.configureStrategy(chain));
            return chain;
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = SingleSignOnBuildingStrategy.BEAN_NAME)
        public SingleSignOnBuildingStrategy singleSignOnBuildingStrategy(
            @Qualifier(TicketRegistrySupport.BEAN_NAME)
            final TicketRegistrySupport ticketRegistrySupport,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService,
            final ConfigurableApplicationContext applicationContext) {
            return new DefaultSingleSignOnBuildingStrategy(ticketRegistrySupport, centralAuthenticationService, applicationContext);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "defaultSingleSignOnParticipationStrategy")
        public SingleSignOnParticipationStrategy defaultSingleSignOnParticipationStrategy(
            final CasConfigurationProperties casProperties,
            @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
            final AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan,
            @Qualifier(TicketRegistrySupport.BEAN_NAME)
            final TicketRegistrySupport ticketRegistrySupport,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            return new DefaultSingleSignOnParticipationStrategy(servicesManager,
                casProperties.getSso(),
                ticketRegistrySupport,
                authenticationServiceSelectionPlan);
        }

        @Bean
        @ConditionalOnMissingBean(name = "defaultSingleSignOnParticipationStrategyConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SingleSignOnParticipationStrategyConfigurer defaultSingleSignOnParticipationStrategyConfigurer(
            @Qualifier("defaultSingleSignOnParticipationStrategy")
            final SingleSignOnParticipationStrategy defaultSingleSignOnParticipationStrategy) {
            return chain -> chain.addStrategy(defaultSingleSignOnParticipationStrategy);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "requiredAuthenticationHandlersSingleSignOnParticipationStrategy")
        public SingleSignOnParticipationStrategy requiredAuthenticationHandlersSingleSignOnParticipationStrategy(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
            final AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(TicketRegistrySupport.BEAN_NAME)
            final TicketRegistrySupport ticketRegistrySupport,
            @Qualifier(AuthenticationEventExecutionPlan.DEFAULT_BEAN_NAME)
            final AuthenticationEventExecutionPlan authenticationEventExecutionPlan) {
            return new RegisteredServiceAuthenticationPolicySingleSignOnParticipationStrategy(servicesManager,
                ticketRegistrySupport, authenticationServiceSelectionPlan,
                authenticationEventExecutionPlan, applicationContext);
        }

        @Bean
        @ConditionalOnMissingBean(name = "requiredAuthenticationHandlersSingleSignOnParticipationStrategyConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SingleSignOnParticipationStrategyConfigurer requiredAuthenticationHandlersSingleSignOnParticipationStrategyConfigurer(
            @Qualifier("requiredAuthenticationHandlersSingleSignOnParticipationStrategy")
            final SingleSignOnParticipationStrategy requiredAuthenticationHandlersSingleSignOnParticipationStrategy) {
            return chain -> chain.addStrategy(requiredAuthenticationHandlersSingleSignOnParticipationStrategy);
        }
    }

    @Configuration(value = "CasCoreWebflowAuthenticationConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreWebflowAuthenticationConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = CasWebflowCredentialProvider.BEAN_NAME)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowCredentialProvider casWebflowCredentialProvider(
            @Qualifier(TenantExtractor.BEAN_NAME)
            final TenantExtractor tenantExtractor) {
            return new DefaultCasWebflowCredentialProvider(tenantExtractor);
        }
    }

    @Configuration(value = "CasCoreWebflowDecoratorsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreWebflowDecoratorsConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "groovyLoginWebflowDecorator")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingGraalVMNativeImage
        public WebflowDecorator groovyLoginWebflowDecorator(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(WebflowDecorator.class)
                .when(BeanCondition.on("cas.webflow.login-decorator.groovy.location")
                    .exists().given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val decorator = casProperties.getWebflow().getLoginDecorator();
                    val groovyScript = decorator.getGroovy().getLocation();
                    LOGGER.trace("Decorating login webflow using [{}]", groovyScript);
                    return new GroovyLoginWebflowDecorator(groovyScript);
                })
                .otherwise(WebflowDecorator::noOp)
                .get();
        }

        @Bean
        @ConditionalOnMissingBean(name = "restfulLoginWebflowDecorator")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public WebflowDecorator restfulLoginWebflowDecorator(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(WebflowDecorator.class)
                .when(BeanCondition.on("cas.webflow.login-decorator.rest.url")
                    .isUrl().given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val decorator = casProperties.getWebflow().getLoginDecorator();
                    LOGGER.trace("Decorating login webflow REST endpoint [{}]", decorator.getRest().getUrl());
                    return new RestfulLoginWebflowDecorator(decorator.getRest());
                })
                .otherwise(WebflowDecorator::noOp)
                .get();
        }
    }
    
    @Configuration(value = "CasCoreWebflowStorageActionsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreWebflowStorageActionsConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_WRITE_BROWSER_STORAGE)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action writeBrowserStorageAction(
            @Qualifier(CasCookieBuilder.BEAN_NAME_TICKET_GRANTING_COOKIE_BUILDER)
            final CasCookieBuilder ticketGrantingTicketCookieGenerator,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new WriteBrowserStorageAction(ticketGrantingTicketCookieGenerator))
                .withId(CasWebflowConstants.ACTION_ID_WRITE_BROWSER_STORAGE)
                .build()
                .get();
        }

        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_READ_BROWSER_STORAGE)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action readBrowserStorageAction(
            @Qualifier(CasCookieBuilder.BEAN_NAME_TICKET_GRANTING_COOKIE_BUILDER)
            final CasCookieBuilder ticketGrantingTicketCookieGenerator,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new ReadBrowserStorageAction(ticketGrantingTicketCookieGenerator))
                .withId(CasWebflowConstants.ACTION_ID_READ_BROWSER_STORAGE)
                .build()
                .get();
        }


        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_PUT_BROWSER_STORAGE)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action putBrowserStorageAction(
            @Qualifier(CasCookieBuilder.BEAN_NAME_TICKET_GRANTING_COOKIE_BUILDER)
            final CasCookieBuilder ticketGrantingTicketCookieGenerator,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new PutBrowserStorageAction(ticketGrantingTicketCookieGenerator))
                .withId(CasWebflowConstants.ACTION_ID_PUT_BROWSER_STORAGE)
                .build()
                .get();
        }

    }
}
