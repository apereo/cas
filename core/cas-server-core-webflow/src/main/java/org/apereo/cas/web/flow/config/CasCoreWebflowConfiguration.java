package org.apereo.cas.web.flow.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationContextValidator;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderAbsentException;
import org.apereo.cas.authentication.MultifactorAuthenticationRequiredException;
import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.authentication.adaptive.UnauthorizedAuthenticationException;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.exceptions.InvalidLoginLocationException;
import org.apereo.cas.authentication.exceptions.InvalidLoginTimeException;
import org.apereo.cas.authentication.exceptions.UniquePrincipalRequiredException;
import org.apereo.cas.authentication.principal.ResponseBuilderLocator;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.web.MessageBundleProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceForPrincipalException;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.ticket.UnsatisfiedAuthenticationPolicyException;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.cipher.WebflowConversationStateCipherExecutor;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.ChainingSingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.DefaultSingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategyConfigurer;
import org.apereo.cas.web.flow.actions.AuthenticationExceptionHandlerAction;
import org.apereo.cas.web.flow.actions.CheckWebAuthenticationRequestAction;
import org.apereo.cas.web.flow.actions.ClearWebflowCredentialAction;
import org.apereo.cas.web.flow.actions.InjectResponseHeadersAction;
import org.apereo.cas.web.flow.actions.RedirectToServiceAction;
import org.apereo.cas.web.flow.actions.RenewAuthenticationRequestCheckAction;
import org.apereo.cas.web.flow.authentication.CasWebflowExceptionCatalog;
import org.apereo.cas.web.flow.authentication.CasWebflowExceptionHandler;
import org.apereo.cas.web.flow.authentication.DefaultCasWebflowAbstractTicketExceptionHandler;
import org.apereo.cas.web.flow.authentication.DefaultCasWebflowAuthenticationExceptionHandler;
import org.apereo.cas.web.flow.authentication.DefaultCasWebflowExceptionCatalog;
import org.apereo.cas.web.flow.authentication.GenericCasWebflowExceptionHandler;
import org.apereo.cas.web.flow.authentication.GroovyCasWebflowAuthenticationExceptionHandler;
import org.apereo.cas.web.flow.authentication.RegisteredServiceAuthenticationPolicySingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
import org.apereo.cas.web.flow.resolver.impl.ServiceTicketRequestWebflowEventResolver;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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

/**
 * This is {@link CasCoreWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "casCoreWebflowConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@AutoConfigureAfter(CasCoreServicesConfiguration.class)
public class CasCoreWebflowConfiguration {


    @Configuration(value = "CasCoreWebflowEventResolutionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreWebflowEventResolutionConfiguration {
        @ConditionalOnMissingBean(name = "serviceTicketRequestWebflowEventResolver")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public CasWebflowEventResolver serviceTicketRequestWebflowEventResolver(
            @Qualifier("casWebflowConfigurationContext")
            final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext) {
            return new ServiceTicketRequestWebflowEventResolver(casWebflowConfigurationContext);
        }


        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
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
    public static class CasCoreWebflowContextConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(TicketRegistrySupport.BEAN_NAME)
            final TicketRegistrySupport ticketRegistrySupport,
            @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
            final AuthenticationSystemSupport authenticationSystemSupport,
            @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
            final AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService,
            @Qualifier("authenticationContextValidator")
            final MultifactorAuthenticationContextValidator authenticationContextValidator,
            @Qualifier(AuthenticationEventExecutionPlan.DEFAULT_BEAN_NAME)
            final AuthenticationEventExecutionPlan authenticationEventExecutionPlan,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier("warnCookieGenerator")
            final CasCookieBuilder warnCookieGenerator,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier("singleSignOnParticipationStrategy")
            final SingleSignOnParticipationStrategy webflowSingleSignOnParticipationStrategy,
            @Qualifier("registeredServiceAccessStrategyEnforcer")
            final AuditableExecution registeredServiceAccessStrategyEnforcer,
            @Qualifier("ticketGrantingTicketCookieGenerator")
            final CasCookieBuilder ticketGrantingTicketCookieGenerator) {
            return CasWebflowEventResolutionConfigurationContext.builder()
                .authenticationContextValidator(authenticationContextValidator)
                .authenticationSystemSupport(authenticationSystemSupport)
                .centralAuthenticationService(centralAuthenticationService)
                .servicesManager(servicesManager)
                .ticketRegistrySupport(ticketRegistrySupport)
                .warnCookieGenerator(warnCookieGenerator)
                .authenticationRequestServiceSelectionStrategies(authenticationServiceSelectionPlan)
                .registeredServiceAccessStrategyEnforcer(registeredServiceAccessStrategyEnforcer)
                .casProperties(casProperties)
                .ticketRegistry(ticketRegistry)
                .singleSignOnParticipationStrategy(webflowSingleSignOnParticipationStrategy)
                .applicationContext(applicationContext)
                .ticketGrantingTicketCookieGenerator(ticketGrantingTicketCookieGenerator)
                .authenticationEventExecutionPlan(authenticationEventExecutionPlan)
                .build();
        }
    }

    @Configuration(value = "CasCoreWebflowActionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreWebflowActionConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_CLEAR_WEBFLOW_CREDENTIALS)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action clearWebflowCredentialsAction() {
            return new ClearWebflowCredentialAction();
        }

        @Bean
        @ConditionalOnMissingBean(name = "checkWebAuthenticationRequestAction")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public Action checkWebAuthenticationRequestAction(final CasConfigurationProperties casProperties) {
            return new CheckWebAuthenticationRequestAction(casProperties.getAuthn().getMfa().getCore().getContentType());
        }

        @Bean
        @ConditionalOnMissingBean(name = "renewAuthenticationRequestCheckAction")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public Action renewAuthenticationRequestCheckAction(
            @Qualifier("singleSignOnParticipationStrategy")
            final SingleSignOnParticipationStrategy singleSignOnParticipationStrategy) {
            return new RenewAuthenticationRequestCheckAction(singleSignOnParticipationStrategy);
        }

        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_REDIRECT_TO_SERVICE)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public Action redirectToServiceAction(
            @Qualifier("webApplicationResponseBuilderLocator")
            final ResponseBuilderLocator responseBuilderLocator) {
            return new RedirectToServiceAction(responseBuilderLocator);
        }

        @Bean
        @ConditionalOnMissingBean(name = "injectResponseHeadersAction")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public Action injectResponseHeadersAction(
            @Qualifier("webApplicationResponseBuilderLocator")
            final ResponseBuilderLocator responseBuilderLocator) {
            return new InjectResponseHeadersAction(responseBuilderLocator);
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_AUTHENTICATION_EXCEPTION_HANDLER)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public Action authenticationExceptionHandler(final List<CasWebflowExceptionHandler> handlers) {
            AnnotationAwareOrderComparator.sort(handlers);
            return new AuthenticationExceptionHandlerAction(handlers);
        }
    }

    @Configuration(value = "CasCoreWebflowExceptionHandlingConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreWebflowExceptionHandlingConfiguration {

        @ConditionalOnMissingBean(name = "groovyCasWebflowAuthenticationExceptionHandler")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        @ConditionalOnProperty(name = "cas.authn.errors.groovy.location")
        public CasWebflowExceptionHandler<Exception> groovyCasWebflowAuthenticationExceptionHandler(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return new GroovyCasWebflowAuthenticationExceptionHandler(
                casProperties.getAuthn().getErrors().getGroovy().getLocation(), applicationContext);
        }

        @ConditionalOnMissingBean(name = "defaultCasWebflowAuthenticationExceptionHandler")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public CasWebflowExceptionHandler<AuthenticationException> defaultCasWebflowAuthenticationExceptionHandler(
            @Qualifier("handledAuthenticationExceptions")
            final CasWebflowExceptionCatalog handledAuthenticationExceptions) {
            return new DefaultCasWebflowAuthenticationExceptionHandler(
                handledAuthenticationExceptions, MessageBundleProperties.DEFAULT_BUNDLE_PREFIX_AUTHN_FAILURE);
        }

        @ConditionalOnMissingBean(name = "defaultCasWebflowAbstractTicketExceptionHandler")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public CasWebflowExceptionHandler<AbstractTicketException> defaultCasWebflowAbstractTicketExceptionHandler(
            @Qualifier("handledAuthenticationExceptions")
            final CasWebflowExceptionCatalog handledAuthenticationExceptions) {
            return new DefaultCasWebflowAbstractTicketExceptionHandler(
                handledAuthenticationExceptions, MessageBundleProperties.DEFAULT_BUNDLE_PREFIX_AUTHN_FAILURE);
        }

        @ConditionalOnMissingBean(name = "genericCasWebflowExceptionHandler")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public CasWebflowExceptionHandler genericCasWebflowExceptionHandler(
            @Qualifier("handledAuthenticationExceptions")
            final CasWebflowExceptionCatalog handledAuthenticationExceptions) {
            return new GenericCasWebflowExceptionHandler(
                handledAuthenticationExceptions, MessageBundleProperties.DEFAULT_BUNDLE_PREFIX_AUTHN_FAILURE);
        }
    }

    @Configuration(value = "CasCoreWebflowExceptionCatalogConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreWebflowExceptionCatalogConfiguration {

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
        @Autowired
        @ConditionalOnMissingBean(name = "handledAuthenticationExceptions")
        public CasWebflowExceptionCatalog handledAuthenticationExceptions(
            final CasConfigurationProperties casProperties) {
            val errors = new DefaultCasWebflowExceptionCatalog();
            errors.registerException(AccountLockedException.class);
            errors.registerException(CredentialExpiredException.class);
            errors.registerException(AccountExpiredException.class);
            errors.registerException(AccountDisabledException.class);
            errors.registerException(InvalidLoginLocationException.class);
            errors.registerException(AccountPasswordMustChangeException.class);
            errors.registerException(InvalidLoginTimeException.class);
            errors.registerException(UniquePrincipalRequiredException.class);

            errors.registerException(AccountNotFoundException.class);
            errors.registerException(FailedLoginException.class);
            errors.registerException(UnauthorizedServiceForPrincipalException.class);
            errors.registerException(PrincipalException.class);
            errors.registerException(UnsatisfiedAuthenticationPolicyException.class);
            errors.registerException(UnauthorizedAuthenticationException.class);
            errors.registerException(MultifactorAuthenticationProviderAbsentException.class);
            errors.registerException(MultifactorAuthenticationRequiredException.class);

            errors.registerExceptions(casProperties.getAuthn().getErrors().getExceptions());
            return errors;
        }
    }

    @Configuration(value = "CasCoreWebflowSingleSignOnConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreWebflowSingleSignOnConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "singleSignOnParticipationStrategy")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public SingleSignOnParticipationStrategy singleSignOnParticipationStrategy(
            final List<SingleSignOnParticipationStrategyConfigurer> providers) {
            AnnotationAwareOrderComparator.sort(providers);
            val chain = new ChainingSingleSignOnParticipationStrategy();
            providers.forEach(provider -> provider.configureStrategy(chain));
            return chain;
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "defaultSingleSignOnParticipationStrategy")
        @Autowired
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
        @Autowired
        public SingleSignOnParticipationStrategyConfigurer defaultSingleSignOnParticipationStrategyConfigurer(
            @Qualifier("defaultSingleSignOnParticipationStrategy")
            final SingleSignOnParticipationStrategy defaultSingleSignOnParticipationStrategy) {
            return chain -> chain.addStrategy(defaultSingleSignOnParticipationStrategy);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "requiredAuthenticationHandlersSingleSignOnParticipationStrategy")
        @Autowired
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
        @Autowired
        public SingleSignOnParticipationStrategyConfigurer requiredAuthenticationHandlersSingleSignOnParticipationStrategyConfigurer(
            @Qualifier("requiredAuthenticationHandlersSingleSignOnParticipationStrategy")
            final SingleSignOnParticipationStrategy requiredAuthenticationHandlersSingleSignOnParticipationStrategy) {
            return chain -> chain.addStrategy(requiredAuthenticationHandlersSingleSignOnParticipationStrategy);
        }
    }
}
