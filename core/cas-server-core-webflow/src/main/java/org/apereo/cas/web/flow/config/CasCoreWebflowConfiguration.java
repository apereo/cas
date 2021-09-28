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
import org.apereo.cas.web.flow.authentication.CasWebflowExceptionHandler;
import org.apereo.cas.web.flow.authentication.DefaultCasWebflowAbstractTicketExceptionHandler;
import org.apereo.cas.web.flow.authentication.DefaultCasWebflowAuthenticationExceptionHandler;
import org.apereo.cas.web.flow.authentication.GenericCasWebflowExceptionHandler;
import org.apereo.cas.web.flow.authentication.GroovyCasWebflowAuthenticationExceptionHandler;
import org.apereo.cas.web.flow.authentication.RegisteredServiceAuthenticationPolicySingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
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
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.webflow.execution.Action;

import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

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
    @ConditionalOnMissingBean(name = "serviceTicketRequestWebflowEventResolver")
    @Bean
    @RefreshScope
    @Autowired
    public CasWebflowEventResolver serviceTicketRequestWebflowEventResolver(
        @Qualifier("casWebflowConfigurationContext")
        final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext) {
        return new ServiceTicketRequestWebflowEventResolver(casWebflowConfigurationContext);
    }

    @Bean
    @RefreshScope
    @Autowired
    public CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier("defaultTicketRegistrySupport")
        final TicketRegistrySupport ticketRegistrySupport,
        @Qualifier("defaultAuthenticationSystemSupport")
        final AuthenticationSystemSupport authenticationSystemSupport,
        @Qualifier("authenticationServiceSelectionPlan")
        final AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan,
        @Qualifier(CentralAuthenticationService.BEAN_NAME)
        final CentralAuthenticationService centralAuthenticationService,
        @Qualifier("authenticationContextValidator")
        final MultifactorAuthenticationContextValidator authenticationContextValidator,
        @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
        final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
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
            .casDelegatingWebflowEventResolver(initialAuthenticationAttemptWebflowEventResolver)
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

    @Bean
    @RefreshScope
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

    @Bean
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_CLEAR_WEBFLOW_CREDENTIALS)
    @RefreshScope
    public Action clearWebflowCredentialsAction() {
        return new ClearWebflowCredentialAction();
    }

    @Bean
    @ConditionalOnMissingBean(name = "checkWebAuthenticationRequestAction")
    @RefreshScope
    @Autowired
    public Action checkWebAuthenticationRequestAction(final CasConfigurationProperties casProperties) {
        return new CheckWebAuthenticationRequestAction(casProperties.getAuthn().getMfa().getCore().getContentType());
    }

    @Bean
    @ConditionalOnMissingBean(name = "renewAuthenticationRequestCheckAction")
    @RefreshScope
    @Autowired
    public Action renewAuthenticationRequestCheckAction(
        @Qualifier("singleSignOnParticipationStrategy")
        final SingleSignOnParticipationStrategy singleSignOnParticipationStrategy) {
        return new RenewAuthenticationRequestCheckAction(singleSignOnParticipationStrategy);
    }

    @Bean
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_REDIRECT_TO_SERVICE)
    @RefreshScope
    @Autowired
    public Action redirectToServiceAction(
        @Qualifier("webApplicationResponseBuilderLocator")
        final ResponseBuilderLocator responseBuilderLocator) {
        return new RedirectToServiceAction(responseBuilderLocator);
    }

    @Bean
    @ConditionalOnMissingBean(name = "injectResponseHeadersAction")
    @RefreshScope
    @Autowired
    public Action injectResponseHeadersAction(
        @Qualifier("webApplicationResponseBuilderLocator")
        final ResponseBuilderLocator responseBuilderLocator) {
        return new InjectResponseHeadersAction(responseBuilderLocator);
    }

    @Bean
    @ConditionalOnMissingBean(name = "singleSignOnParticipationStrategy")
    @RefreshScope
    @Autowired
    public SingleSignOnParticipationStrategy singleSignOnParticipationStrategy(
        final ConfigurableApplicationContext applicationContext) {
        val resolvers = applicationContext.getBeansOfType(SingleSignOnParticipationStrategyConfigurer.class, false, true);
        val providers = new ArrayList<SingleSignOnParticipationStrategyConfigurer>(resolvers.values());
        AnnotationAwareOrderComparator.sort(providers);
        val chain = new ChainingSingleSignOnParticipationStrategy();
        providers.forEach(provider -> provider.configureStrategy(chain));
        return chain;
    }

    @ConditionalOnMissingBean(name = "groovyCasWebflowAuthenticationExceptionHandler")
    @Bean
    @RefreshScope
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
    @RefreshScope
    @Autowired
    public CasWebflowExceptionHandler<AuthenticationException> defaultCasWebflowAuthenticationExceptionHandler(
        @Qualifier("handledAuthenticationExceptions")
        final Set<Class<? extends Throwable>> handledAuthenticationExceptions) {
        return new DefaultCasWebflowAuthenticationExceptionHandler(
            handledAuthenticationExceptions, MessageBundleProperties.DEFAULT_BUNDLE_PREFIX_AUTHN_FAILURE);
    }

    @ConditionalOnMissingBean(name = "defaultCasWebflowAbstractTicketExceptionHandler")
    @Bean
    @RefreshScope
    @Autowired
    public CasWebflowExceptionHandler<AbstractTicketException> defaultCasWebflowAbstractTicketExceptionHandler(
        @Qualifier("handledAuthenticationExceptions")
        final Set<Class<? extends Throwable>> handledAuthenticationExceptions) {
        return new DefaultCasWebflowAbstractTicketExceptionHandler(
            handledAuthenticationExceptions, MessageBundleProperties.DEFAULT_BUNDLE_PREFIX_AUTHN_FAILURE);
    }

    @ConditionalOnMissingBean(name = "genericCasWebflowExceptionHandler")
    @Bean
    @RefreshScope
    @Autowired
    public CasWebflowExceptionHandler genericCasWebflowExceptionHandler(
        @Qualifier("handledAuthenticationExceptions")
        final Set<Class<? extends Throwable>> handledAuthenticationExceptions) {
        return new GenericCasWebflowExceptionHandler(
            handledAuthenticationExceptions, MessageBundleProperties.DEFAULT_BUNDLE_PREFIX_AUTHN_FAILURE);
    }


    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_AUTHENTICATION_EXCEPTION_HANDLER)
    @Bean
    @RefreshScope
    @Autowired
    public Action authenticationExceptionHandler(final ConfigurableApplicationContext applicationContext) {
        val beans = applicationContext.getBeansOfType(CasWebflowExceptionHandler.class, false, true);
        val handlers = new ArrayList<CasWebflowExceptionHandler>(beans.values());
        AnnotationAwareOrderComparator.sort(handlers);
        return new AuthenticationExceptionHandlerAction(handlers);
    }

    @RefreshScope
    @Bean
    @Autowired
    @ConditionalOnMissingBean(name = "handledAuthenticationExceptions")
    public Set<Class<? extends Throwable>> handledAuthenticationExceptions(
        final CasConfigurationProperties casProperties) {
        /*
         * Order is important here; We want the account policy exceptions to be handled
         * first before moving onto more generic errors. In the event that multiple handlers
         * are defined, where one fails due to account policy restriction and one fails
         * due to a bad password, we want the error associated with the account policy
         * to be processed first, rather than presenting a more generic error associated
         */
        val errors = new LinkedHashSet<Class<? extends Throwable>>();
        errors.add(AccountLockedException.class);
        errors.add(CredentialExpiredException.class);
        errors.add(AccountExpiredException.class);
        errors.add(AccountDisabledException.class);
        errors.add(InvalidLoginLocationException.class);
        errors.add(AccountPasswordMustChangeException.class);
        errors.add(InvalidLoginTimeException.class);
        errors.add(UniquePrincipalRequiredException.class);

        errors.add(AccountNotFoundException.class);
        errors.add(FailedLoginException.class);
        errors.add(UnauthorizedServiceForPrincipalException.class);
        errors.add(PrincipalException.class);
        errors.add(UnsatisfiedAuthenticationPolicyException.class);
        errors.add(UnauthorizedAuthenticationException.class);
        errors.add(MultifactorAuthenticationProviderAbsentException.class);
        errors.add(MultifactorAuthenticationRequiredException.class);

        errors.addAll(casProperties.getAuthn().getErrors().getExceptions());
        return errors;
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "defaultSingleSignOnParticipationStrategy")
    @Autowired
    public SingleSignOnParticipationStrategy defaultSingleSignOnParticipationStrategy(
        final CasConfigurationProperties casProperties,
        @Qualifier("authenticationServiceSelectionPlan")
        final AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan,
        @Qualifier("defaultTicketRegistrySupport")
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
    @RefreshScope
    @Autowired
    public SingleSignOnParticipationStrategyConfigurer defaultSingleSignOnParticipationStrategyConfigurer(
        @Qualifier("defaultSingleSignOnParticipationStrategy")
        final SingleSignOnParticipationStrategy defaultSingleSignOnParticipationStrategy) {
        return chain -> chain.addStrategy(defaultSingleSignOnParticipationStrategy);
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "requiredAuthenticationHandlersSingleSignOnParticipationStrategy")
    @Autowired
    public SingleSignOnParticipationStrategy requiredAuthenticationHandlersSingleSignOnParticipationStrategy(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("authenticationServiceSelectionPlan")
        final AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        @Qualifier("defaultTicketRegistrySupport")
        final TicketRegistrySupport ticketRegistrySupport,
        @Qualifier(AuthenticationEventExecutionPlan.DEFAULT_BEAN_NAME)
        final AuthenticationEventExecutionPlan authenticationEventExecutionPlan) {
        return new RegisteredServiceAuthenticationPolicySingleSignOnParticipationStrategy(servicesManager,
            ticketRegistrySupport, authenticationServiceSelectionPlan,
            authenticationEventExecutionPlan, applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean(name = "requiredAuthenticationHandlersSingleSignOnParticipationStrategyConfigurer")
    @RefreshScope
    @Autowired
    public SingleSignOnParticipationStrategyConfigurer requiredAuthenticationHandlersSingleSignOnParticipationStrategyConfigurer(
        @Qualifier("requiredAuthenticationHandlersSingleSignOnParticipationStrategy")
        final SingleSignOnParticipationStrategy requiredAuthenticationHandlersSingleSignOnParticipationStrategy) {
        return chain -> chain.addStrategy(requiredAuthenticationHandlersSingleSignOnParticipationStrategy);
    }
}
