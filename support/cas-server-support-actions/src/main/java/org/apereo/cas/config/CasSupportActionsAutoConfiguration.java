package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.logout.LogoutConfirmationResolver;
import org.apereo.cas.logout.LogoutExecutionPlan;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.logout.slo.SingleLogoutRequestExecutor;
import org.apereo.cas.services.RegisteredServicePrincipalAccessStrategyEnforcer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.ServiceTicketGeneratorAuthority;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.scripting.ScriptResourceCacheManager;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.FlowExecutionExceptionResolver;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowCredentialProvider;
import org.apereo.cas.web.flow.GatewayServicesManagementCheckAction;
import org.apereo.cas.web.flow.GenerateServiceTicketAction;
import org.apereo.cas.web.flow.PopulateSpringSecurityContextAction;
import org.apereo.cas.web.flow.ServiceAuthorizationCheckAction;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.account.AccountProfileRemoveSingleSignOnSessionAction;
import org.apereo.cas.web.flow.account.PrepareAccountProfileViewAction;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import org.apereo.cas.web.flow.actions.FetchTicketGrantingTicketAction;
import org.apereo.cas.web.flow.actions.InitialAuthenticationAction;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;
import org.apereo.cas.web.flow.login.CreateTicketGrantingTicketAction;
import org.apereo.cas.web.flow.login.GenericSuccessViewAction;
import org.apereo.cas.web.flow.login.InitialAuthenticationRequestValidationAction;
import org.apereo.cas.web.flow.login.InitialFlowSetupAction;
import org.apereo.cas.web.flow.login.InitializeLoginAction;
import org.apereo.cas.web.flow.login.RedirectUnauthorizedServiceUrlAction;
import org.apereo.cas.web.flow.login.SendTicketGrantingTicketAction;
import org.apereo.cas.web.flow.login.ServiceWarningAction;
import org.apereo.cas.web.flow.login.SetServiceUnauthorizedRedirectUrlAction;
import org.apereo.cas.web.flow.login.TicketGrantingTicketCheckAction;
import org.apereo.cas.web.flow.login.VerifyRequiredServiceAction;
import org.apereo.cas.web.flow.logout.ConfirmLogoutAction;
import org.apereo.cas.web.flow.logout.FinishLogoutAction;
import org.apereo.cas.web.flow.logout.FrontChannelLogoutAction;
import org.apereo.cas.web.flow.logout.LogoutAction;
import org.apereo.cas.web.flow.logout.LogoutViewSetupAction;
import org.apereo.cas.web.flow.logout.TerminateSessionAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.webflow.execution.Action;
import java.util.List;

/**
 * This is {@link CasSupportActionsAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = false)
@AutoConfiguration
public class CasSupportActionsAutoConfiguration {

    @Configuration(value = "CasSupportActionsExceptionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasSupportActionsExceptionConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public HandlerExceptionResolver errorHandlerResolver() {
            return new FlowExecutionExceptionResolver();
        }
    }

    @Configuration(value = "CasSupportActionsExecutionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasSupportActionsExecutionConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_FETCH_TICKET_GRANTING_TICKET)
        public Action fetchTicketGrantingTicketAction(
            final CasConfigurationProperties casProperties,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasCookieBuilder.BEAN_NAME_TICKET_GRANTING_COOKIE_BUILDER)
            final CasCookieBuilder ticketGrantingTicketCookieGenerator) {

            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new FetchTicketGrantingTicketAction(ticketRegistry, ticketGrantingTicketCookieGenerator))
                .withId(CasWebflowConstants.ACTION_ID_FETCH_TICKET_GRANTING_TICKET)
                .build()
                .get();
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_AUTHENTICATION_VIA_FORM_ACTION)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action authenticationViaFormAction(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowEventResolver.BEAN_NAME_SERVICE_TICKET_EVENT_RESOLVER)
            final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
            @Qualifier(CasDelegatingWebflowEventResolver.BEAN_NAME_INITIAL_AUTHENTICATION_EVENT_RESOLVER)
            final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
            @Qualifier(AdaptiveAuthenticationPolicy.BEAN_NAME)
            final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy) {

            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new InitialAuthenticationAction(initialAuthenticationAttemptWebflowEventResolver,
                    serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy))
                .withId(CasWebflowConstants.ACTION_ID_AUTHENTICATION_VIA_FORM_ACTION)
                .build()
                .get();
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_SERVICE_AUTHZ_CHECK)
        @Bean
        public Action serviceAuthorizationCheck(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
            final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies) {

            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new ServiceAuthorizationCheckAction(servicesManager, authenticationRequestServiceSelectionStrategies))
                .withId(CasWebflowConstants.ACTION_ID_SERVICE_AUTHZ_CHECK)
                .build()
                .get();
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_SINGLE_SIGON_SESSION_CREATED)
        @Bean
        public Action singleSignOnSessionCreated(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> ConsumerExecutionAction.NONE)
                .withId(CasWebflowConstants.ACTION_ID_SINGLE_SIGON_SESSION_CREATED)
                .build()
                .get();
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_SEND_TICKET_GRANTING_TICKET)
        @Bean
        public Action sendTicketGrantingTicketAction(
            final CasConfigurationProperties casProperties,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasCookieBuilder.BEAN_NAME_TICKET_GRANTING_COOKIE_BUILDER)
            final CasCookieBuilder ticketGrantingTicketCookieGenerator,
            @Qualifier(SingleSignOnParticipationStrategy.BEAN_NAME)
            final SingleSignOnParticipationStrategy webflowSingleSignOnParticipationStrategy) {

            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new SendTicketGrantingTicketAction(ticketRegistry, ticketGrantingTicketCookieGenerator,
                    webflowSingleSignOnParticipationStrategy))
                .withId(CasWebflowConstants.ACTION_ID_SEND_TICKET_GRANTING_TICKET)
                .build()
                .get();
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_CREATE_TICKET_GRANTING_TICKET)
        @Bean
        public Action createTicketGrantingTicketAction(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowEventResolutionConfigurationContext.BEAN_NAME)
            final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new CreateTicketGrantingTicketAction(casWebflowConfigurationContext))
                .withId(CasWebflowConstants.ACTION_ID_CREATE_TICKET_GRANTING_TICKET)
                .build()
                .get();
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_FINISH_LOGOUT)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action finishLogoutAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(CasCookieBuilder.BEAN_NAME_TICKET_GRANTING_COOKIE_BUILDER)
            final CasCookieBuilder ticketGrantingTicketCookieGenerator,
            @Qualifier(ArgumentExtractor.BEAN_NAME)
            final ArgumentExtractor argumentExtractor,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier(LogoutExecutionPlan.BEAN_NAME)
            final LogoutExecutionPlan logoutExecutionPlan) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new FinishLogoutAction(ticketRegistry, ticketGrantingTicketCookieGenerator,
                    argumentExtractor, servicesManager, logoutExecutionPlan, casProperties))
                .withId(CasWebflowConstants.ACTION_ID_FINISH_LOGOUT)
                .build()
                .get();
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_LOGOUT)
        public Action logoutAction(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(CasCookieBuilder.BEAN_NAME_TICKET_GRANTING_COOKIE_BUILDER)
            final CasCookieBuilder ticketGrantingTicketCookieGenerator,
            @Qualifier(ArgumentExtractor.BEAN_NAME)
            final ArgumentExtractor argumentExtractor,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier(LogoutExecutionPlan.BEAN_NAME)
            final LogoutExecutionPlan logoutExecutionPlan) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new LogoutAction(ticketRegistry, ticketGrantingTicketCookieGenerator,
                    argumentExtractor, servicesManager, logoutExecutionPlan, casProperties))
                .withId(CasWebflowConstants.ACTION_ID_LOGOUT)
                .build()
                .get();
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_INIT_LOGIN_ACTION)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action initializeLoginAction(final CasConfigurationProperties casProperties,
                                            final ConfigurableApplicationContext applicationContext,
                                            @Qualifier(ServicesManager.BEAN_NAME)
                                            final ServicesManager servicesManager) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new InitializeLoginAction(servicesManager, casProperties))
                .withId(CasWebflowConstants.ACTION_ID_INIT_LOGIN_ACTION)
                .build()
                .get();
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_SET_SERVICE_UNAUTHORIZED_REDIRECT_URL)
        @Bean
        public Action setServiceUnauthorizedRedirectUrlAction(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new SetServiceUnauthorizedRedirectUrlAction(servicesManager))
                .withId(CasWebflowConstants.ACTION_ID_SET_SERVICE_UNAUTHORIZED_REDIRECT_URL)
                .build()
                .get();
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_INITIAL_FLOW_SETUP)
        public Action initialFlowSetupAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("authenticationEventExecutionPlan")
            final AuthenticationEventExecutionPlan authenticationEventExecutionPlan,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(CasCookieBuilder.BEAN_NAME_TICKET_GRANTING_COOKIE_BUILDER)
            final CasCookieBuilder ticketGrantingTicketCookieGenerator,
            @Qualifier(CasCookieBuilder.BEAN_NAME_WARN_COOKIE_BUILDER)
            final CasCookieBuilder warnCookieGenerator,
            @Qualifier(TicketRegistrySupport.BEAN_NAME)
            final TicketRegistrySupport ticketRegistrySupport,
            @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
            final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies,
            @Qualifier(SingleSignOnParticipationStrategy.BEAN_NAME)
            final SingleSignOnParticipationStrategy webflowSingleSignOnParticipationStrategy,
            @Qualifier(ArgumentExtractor.BEAN_NAME)
            final ArgumentExtractor argumentExtractor) {

            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new InitialFlowSetupAction(CollectionUtils.wrap(argumentExtractor), servicesManager,
                    authenticationRequestServiceSelectionStrategies, ticketGrantingTicketCookieGenerator,
                    warnCookieGenerator, casProperties, authenticationEventExecutionPlan,
                    webflowSingleSignOnParticipationStrategy, ticketRegistrySupport))
                .withId(CasWebflowConstants.ACTION_ID_INITIAL_FLOW_SETUP)
                .build()
                .get();
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_VERIFY_REQUIRED_SERVICE)
        public Action verifyRequiredServiceAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(CasCookieBuilder.BEAN_NAME_TICKET_GRANTING_COOKIE_BUILDER)
            final CasCookieBuilder ticketGrantingTicketCookieGenerator,
            @Qualifier(TicketRegistrySupport.BEAN_NAME)
            final TicketRegistrySupport ticketRegistrySupport) {

            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new VerifyRequiredServiceAction(servicesManager,
                    ticketGrantingTicketCookieGenerator, casProperties, ticketRegistrySupport))
                .withId(CasWebflowConstants.ACTION_ID_VERIFY_REQUIRED_SERVICE)
                .build()
                .get();
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_INITIAL_AUTHN_REQUEST_VALIDATION)
        public Action initialAuthenticationRequestValidationAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("rankedAuthenticationProviderWebflowEventResolver")
            final CasWebflowEventResolver rankedAuthenticationProviderWebflowEventResolver) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new InitialAuthenticationRequestValidationAction(rankedAuthenticationProviderWebflowEventResolver))
                .withId(CasWebflowConstants.ACTION_ID_INITIAL_AUTHN_REQUEST_VALIDATION)
                .build()
                .get();
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_GENERIC_SUCCESS_VIEW)
        public Action genericSuccessViewAction(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            final CasConfigurationProperties casProperties,
            @Qualifier(RegisteredServicePrincipalAccessStrategyEnforcer.BEAN_NAME)
            final RegisteredServicePrincipalAccessStrategyEnforcer principalAccessStrategyEnforcer,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new GenericSuccessViewAction(ticketRegistry, servicesManager,
                    webApplicationServiceFactory, casProperties, principalAccessStrategyEnforcer))
                .withId(CasWebflowConstants.ACTION_ID_GENERIC_SUCCESS_VIEW)
                .build()
                .get();
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_REDIRECT_UNAUTHORIZED_SERVICE_URL)
        public Action redirectUnauthorizedServiceUrlAction(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(ScriptResourceCacheManager.BEAN_NAME)
            final ObjectProvider<ScriptResourceCacheManager> scriptResourceCacheManager,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new RedirectUnauthorizedServiceUrlAction(servicesManager, scriptResourceCacheManager))
                .withId(CasWebflowConstants.ACTION_ID_REDIRECT_UNAUTHORIZED_SERVICE_URL)
                .build()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_GENERATE_SERVICE_TICKET)
        public Action generateServiceTicketAction(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService,
            @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
            final AuthenticationSystemSupport authenticationSystemSupport,
            @Qualifier(TicketRegistrySupport.BEAN_NAME)
            final TicketRegistrySupport ticketRegistrySupport,
            @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
            final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies,
            final List<ServiceTicketGeneratorAuthority> serviceTicketAuthorities,
            @Qualifier(CasWebflowCredentialProvider.BEAN_NAME)
            final CasWebflowCredentialProvider casWebflowCredentialProvider) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new GenerateServiceTicketAction(authenticationSystemSupport, centralAuthenticationService,
                    ticketRegistrySupport, authenticationRequestServiceSelectionStrategies,
                    servicesManager, serviceTicketAuthorities, casWebflowCredentialProvider))
                .withId(CasWebflowConstants.ACTION_ID_GENERATE_SERVICE_TICKET)
                .build()
                .get();
        }

        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_GATEWAY_SERVICES_MANAGEMENT)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action gatewayServicesManagementCheck(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
            final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies) {

            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new GatewayServicesManagementCheckAction(servicesManager, authenticationRequestServiceSelectionStrategies))
                .withId(CasWebflowConstants.ACTION_ID_GATEWAY_SERVICES_MANAGEMENT)
                .build()
                .get();
        }

        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_FRONT_CHANNEL_LOGOUT)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action frontChannelLogoutAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(CasCookieBuilder.BEAN_NAME_TICKET_GRANTING_COOKIE_BUILDER)
            final CasCookieBuilder ticketGrantingTicketCookieGenerator,
            @Qualifier(ArgumentExtractor.BEAN_NAME)
            final ArgumentExtractor argumentExtractor,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier(LogoutExecutionPlan.BEAN_NAME)
            final LogoutExecutionPlan logoutExecutionPlan) {

            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new FrontChannelLogoutAction(ticketRegistry,
                    ticketGrantingTicketCookieGenerator, argumentExtractor,
                    servicesManager, logoutExecutionPlan, casProperties))
                .withId(CasWebflowConstants.ACTION_ID_FRONT_CHANNEL_LOGOUT)
                .build()
                .get();
        }

        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_TICKET_GRANTING_TICKET_CHECK)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action ticketGrantingTicketCheckAction(
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new TicketGrantingTicketCheckAction(ticketRegistry))
                .withId(CasWebflowConstants.ACTION_ID_TICKET_GRANTING_TICKET_CHECK)
                .build()
                .get();
        }

        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_TERMINATE_SESSION)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action terminateSessionAction(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(LogoutConfirmationResolver.DEFAULT_BEAN_NAME)
            final LogoutConfirmationResolver logoutConfirmationResolver,
            @Qualifier(LogoutManager.DEFAULT_BEAN_NAME)
            final LogoutManager logoutManager,
            @Qualifier(CasCookieBuilder.BEAN_NAME_TICKET_GRANTING_COOKIE_BUILDER)
            final CasCookieBuilder ticketGrantingTicketCookieGenerator,
            @Qualifier(CasCookieBuilder.BEAN_NAME_WARN_COOKIE_BUILDER)
            final CasCookieBuilder warnCookieGenerator,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService,
            @Qualifier(SingleLogoutRequestExecutor.BEAN_NAME)
            final SingleLogoutRequestExecutor defaultSingleLogoutRequestExecutor) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new TerminateSessionAction(centralAuthenticationService, ticketGrantingTicketCookieGenerator,
                    warnCookieGenerator, casProperties.getLogout(), logoutManager,
                    defaultSingleLogoutRequestExecutor, logoutConfirmationResolver))
                .withId(CasWebflowConstants.ACTION_ID_TERMINATE_SESSION)
                .build()
                .get();
        }

        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_CONFIRM_LOGOUT)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action confirmLogoutAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(CasCookieBuilder.BEAN_NAME_TICKET_GRANTING_COOKIE_BUILDER)
            final CasCookieBuilder ticketGrantingTicketCookieGenerator,
            @Qualifier(ArgumentExtractor.BEAN_NAME)
            final ArgumentExtractor argumentExtractor,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier(LogoutExecutionPlan.BEAN_NAME)
            final LogoutExecutionPlan logoutExecutionPlan) {

            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new ConfirmLogoutAction(ticketRegistry, ticketGrantingTicketCookieGenerator,
                    argumentExtractor, servicesManager, logoutExecutionPlan, casProperties))
                .withId(CasWebflowConstants.ACTION_ID_CONFIRM_LOGOUT)
                .build()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_LOGOUT_VIEW_SETUP)
        public Action logoutViewSetupAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(CasCookieBuilder.BEAN_NAME_TICKET_GRANTING_COOKIE_BUILDER)
            final CasCookieBuilder ticketGrantingTicketCookieGenerator,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier(ArgumentExtractor.BEAN_NAME)
            final ArgumentExtractor argumentExtractor,
            @Qualifier(LogoutExecutionPlan.BEAN_NAME)
            final LogoutExecutionPlan logoutExecutionPlan) {

            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new LogoutViewSetupAction(ticketRegistry,
                    ticketGrantingTicketCookieGenerator, argumentExtractor,
                    servicesManager, logoutExecutionPlan, casProperties))
                .withId(CasWebflowConstants.ACTION_ID_LOGOUT_VIEW_SETUP)
                .build()
                .get();
        }

        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_SERVICE_WARNING)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action serviceWarningAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(CasCookieBuilder.BEAN_NAME_WARN_COOKIE_BUILDER)
            final CasCookieBuilder warnCookieGenerator,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService,
            @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
            final AuthenticationSystemSupport authenticationSystemSupport,
            @Qualifier(TicketRegistrySupport.BEAN_NAME)
            final TicketRegistrySupport ticketRegistrySupport,
            final List<ServiceTicketGeneratorAuthority> serviceTicketAuthorities,
            @Qualifier(CasWebflowCredentialProvider.BEAN_NAME)
            final CasWebflowCredentialProvider casWebflowCredentialProvider) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new ServiceWarningAction(centralAuthenticationService, authenticationSystemSupport,
                    ticketRegistrySupport, warnCookieGenerator, serviceTicketAuthorities,
                    casWebflowCredentialProvider))
                .withId(CasWebflowConstants.ACTION_ID_SERVICE_WARNING)
                .build()
                .get();
        }

        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_POPULATE_SECURITY_CONTEXT)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action populateSpringSecurityContextAction(
            @Qualifier("securityContextRepository")
            final ObjectProvider<SecurityContextRepository> securityContextRepository,
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new PopulateSpringSecurityContextAction(securityContextRepository))
                .withId(CasWebflowConstants.ACTION_ID_POPULATE_SECURITY_CONTEXT)
                .build()
                .get();
        }
    }

    @Configuration(value = "CasSupportActionsAccountProfileConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.AccountManagement, enabledByDefault = false)
    static class CasSupportActionsAccountProfileConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_REMOVE_SINGLE_SIGNON_SESSION)
        public Action accountProfileRemoveSingleSignOnSessionAction(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            final CasConfigurationProperties casProperties) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new AccountProfileRemoveSingleSignOnSessionAction(ticketRegistry))
                .withId(CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_REMOVE_SINGLE_SIGNON_SESSION)
                .build()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_PREPARE_ACCOUNT_PROFILE)
        public Action prepareAccountProfileViewAction(
            @Qualifier(GeoLocationService.BEAN_NAME)
            final ObjectProvider<GeoLocationService> geoLocationService,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(AuditTrailExecutionPlan.BEAN_NAME)
            final AuditTrailExecutionPlan auditTrailExecutionPlan,
            @Qualifier(RegisteredServicePrincipalAccessStrategyEnforcer.BEAN_NAME)
            final RegisteredServicePrincipalAccessStrategyEnforcer principalAccessStrategyEnforcer,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            final CasConfigurationProperties casProperties) {

            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new PrepareAccountProfileViewAction(ticketRegistry,
                    servicesManager, casProperties, auditTrailExecutionPlan,
                    geoLocationService.getIfAvailable(), principalAccessStrategyEnforcer))
                .withId(CasWebflowConstants.ACTION_ID_PREPARE_ACCOUNT_PROFILE)
                .build()
                .get();
        }
    }
}
