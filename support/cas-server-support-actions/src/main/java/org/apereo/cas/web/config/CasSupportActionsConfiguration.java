package org.apereo.cas.web.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.PrincipalElectionStrategy;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.LogoutExecutionPlan;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.logout.slo.SingleLogoutRequestExecutor;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.FlowExecutionExceptionResolver;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.GatewayServicesManagementCheckAction;
import org.apereo.cas.web.flow.GenerateServiceTicketAction;
import org.apereo.cas.web.flow.ServiceAuthorizationCheckAction;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.actions.InitialAuthenticationAction;
import org.apereo.cas.web.flow.login.CreateTicketGrantingTicketAction;
import org.apereo.cas.web.flow.login.GenericSuccessViewAction;
import org.apereo.cas.web.flow.login.InitialAuthenticationRequestValidationAction;
import org.apereo.cas.web.flow.login.InitialFlowSetupAction;
import org.apereo.cas.web.flow.login.InitializeLoginAction;
import org.apereo.cas.web.flow.login.RedirectUnauthorizedServiceUrlAction;
import org.apereo.cas.web.flow.login.RenderLoginAction;
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link CasSupportActionsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "casSupportActionsConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement
public class CasSupportActionsConfiguration {

    @Configuration(value = "CasSupportActionsExceptionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasSupportActionsExceptionConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public HandlerExceptionResolver errorHandlerResolver() {
            return new FlowExecutionExceptionResolver();
        }
    }


    @Configuration(value = "CasSupportActionsExecutionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasSupportActionsExecutionConfiguration {

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_AUTHENTICATION_VIA_FORM_ACTION)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action authenticationViaFormAction(
            @Qualifier("serviceTicketRequestWebflowEventResolver")
            final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
            @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
            final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
            @Qualifier("adaptiveAuthenticationPolicy")
            final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy) {
            return new InitialAuthenticationAction(initialAuthenticationAttemptWebflowEventResolver, serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_SERVICE_AUTHZ_CHECK)
        @Bean
        @Autowired
        public Action serviceAuthorizationCheck(
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
            final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies) {
            return new ServiceAuthorizationCheckAction(servicesManager, authenticationRequestServiceSelectionStrategies);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_SEND_TICKET_GRANTING_TICKET)
        @Bean
        public Action sendTicketGrantingTicketAction(
            @Qualifier("ticketGrantingTicketCookieGenerator")
            final CasCookieBuilder ticketGrantingTicketCookieGenerator,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService,
            @Qualifier("singleSignOnParticipationStrategy")
            final SingleSignOnParticipationStrategy webflowSingleSignOnParticipationStrategy) {
            return new SendTicketGrantingTicketAction(centralAuthenticationService,
                ticketGrantingTicketCookieGenerator, webflowSingleSignOnParticipationStrategy);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_CREATE_TICKET_GRANTING_TICKET)
        @Bean
        public Action createTicketGrantingTicketAction(
            @Qualifier("casWebflowConfigurationContext")
            final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext) {
            return new CreateTicketGrantingTicketAction(casWebflowConfigurationContext);
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_FINISH_LOGOUT)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public Action finishLogoutAction(final CasConfigurationProperties casProperties,
                                         @Qualifier(ServicesManager.BEAN_NAME)
                                         final ServicesManager servicesManager,
                                         @Qualifier("ticketGrantingTicketCookieGenerator")
                                         final CasCookieBuilder ticketGrantingTicketCookieGenerator,
                                         @Qualifier(CentralAuthenticationService.BEAN_NAME)
                                         final CentralAuthenticationService centralAuthenticationService,
                                         @Qualifier("argumentExtractor")
                                         final ArgumentExtractor argumentExtractor,
                                         @Qualifier("logoutExecutionPlan")
                                         final LogoutExecutionPlan logoutExecutionPlan) {
            return new FinishLogoutAction(centralAuthenticationService, ticketGrantingTicketCookieGenerator,
                argumentExtractor, servicesManager, logoutExecutionPlan, casProperties);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_LOGOUT)
        @Autowired
        public Action logoutAction(final CasConfigurationProperties casProperties,
                                   @Qualifier(ServicesManager.BEAN_NAME)
                                   final ServicesManager servicesManager,
                                   @Qualifier("ticketGrantingTicketCookieGenerator")
                                   final CasCookieBuilder ticketGrantingTicketCookieGenerator,
                                   @Qualifier(CentralAuthenticationService.BEAN_NAME)
                                   final CentralAuthenticationService centralAuthenticationService,
                                   @Qualifier("argumentExtractor")
                                   final ArgumentExtractor argumentExtractor,
                                   @Qualifier("logoutExecutionPlan")
                                   final LogoutExecutionPlan logoutExecutionPlan) {
            return new LogoutAction(centralAuthenticationService, ticketGrantingTicketCookieGenerator,
                argumentExtractor, servicesManager, logoutExecutionPlan, casProperties);
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_INIT_LOGIN_ACTION)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public Action initializeLoginAction(final CasConfigurationProperties casProperties,
                                            @Qualifier(ServicesManager.BEAN_NAME)
                                            final ServicesManager servicesManager) {
            return new InitializeLoginAction(servicesManager, casProperties);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "setServiceUnauthorizedRedirectUrlAction")
        @Bean
        public Action setServiceUnauthorizedRedirectUrlAction(
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            return new SetServiceUnauthorizedRedirectUrlAction(servicesManager);
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_RENDER_LOGIN_FORM)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public Action renderLoginFormAction(final CasConfigurationProperties casProperties,
                                            final ConfigurableApplicationContext applicationContext,
                                            @Qualifier(ServicesManager.BEAN_NAME)
                                            final ServicesManager servicesManager) {
            return new RenderLoginAction(servicesManager, casProperties, applicationContext);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_INITIAL_FLOW_SETUP)
        @Autowired
        public Action initialFlowSetupAction(final CasConfigurationProperties casProperties,
                                             @Qualifier("authenticationEventExecutionPlan")
                                             final AuthenticationEventExecutionPlan authenticationEventExecutionPlan,
                                             @Qualifier(ServicesManager.BEAN_NAME)
                                             final ServicesManager servicesManager,
                                             @Qualifier("ticketGrantingTicketCookieGenerator")
                                             final CasCookieBuilder ticketGrantingTicketCookieGenerator,
                                             @Qualifier("warnCookieGenerator")
                                             final CasCookieBuilder warnCookieGenerator,
                                             @Qualifier(TicketRegistrySupport.BEAN_NAME)
                                             final TicketRegistrySupport ticketRegistrySupport,
                                             @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
                                             final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies,
                                             @Qualifier("singleSignOnParticipationStrategy")
                                             final SingleSignOnParticipationStrategy webflowSingleSignOnParticipationStrategy,
                                             @Qualifier("argumentExtractor")
                                             final ArgumentExtractor argumentExtractor) {
            return new InitialFlowSetupAction(CollectionUtils.wrap(argumentExtractor), servicesManager,
                authenticationRequestServiceSelectionStrategies, ticketGrantingTicketCookieGenerator,
                warnCookieGenerator, casProperties, authenticationEventExecutionPlan,
                webflowSingleSignOnParticipationStrategy, ticketRegistrySupport);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_VERIFY_REQUIRED_SERVICE)
        @Autowired
        public Action verifyRequiredServiceAction(final CasConfigurationProperties casProperties,
                                                  @Qualifier(ServicesManager.BEAN_NAME)
                                                  final ServicesManager servicesManager,
                                                  @Qualifier("ticketGrantingTicketCookieGenerator")
                                                  final CasCookieBuilder ticketGrantingTicketCookieGenerator,
                                                  @Qualifier(TicketRegistrySupport.BEAN_NAME)
                                                  final TicketRegistrySupport ticketRegistrySupport) {
            return new VerifyRequiredServiceAction(servicesManager, ticketGrantingTicketCookieGenerator, casProperties, ticketRegistrySupport);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_INITIAL_AUTHN_REQUEST_VALIDATION)
        public Action initialAuthenticationRequestValidationAction(
            @Qualifier("rankedAuthenticationProviderWebflowEventResolver")
            final CasWebflowEventResolver rankedAuthenticationProviderWebflowEventResolver) {
            return new InitialAuthenticationRequestValidationAction(rankedAuthenticationProviderWebflowEventResolver);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "genericSuccessViewAction")
        @Autowired
        public Action genericSuccessViewAction(final CasConfigurationProperties casProperties,
                                               @Qualifier(ServicesManager.BEAN_NAME)
                                               final ServicesManager servicesManager,
                                               @Qualifier("webApplicationServiceFactory")
                                               final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
                                               @Qualifier(CentralAuthenticationService.BEAN_NAME)
                                               final CentralAuthenticationService centralAuthenticationService) {
            return new GenericSuccessViewAction(centralAuthenticationService, servicesManager,
                webApplicationServiceFactory, casProperties);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "redirectUnauthorizedServiceUrlAction")
        @Autowired
        public Action redirectUnauthorizedServiceUrlAction(final ConfigurableApplicationContext applicationContext,
                                                           @Qualifier(ServicesManager.BEAN_NAME)
                                                           final ServicesManager servicesManager) {
            return new RedirectUnauthorizedServiceUrlAction(servicesManager, applicationContext);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_GENERATE_SERVICE_TICKET)
        public Action generateServiceTicketAction(
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
            @Qualifier("principalElectionStrategy")
            final PrincipalElectionStrategy principalElectionStrategy) {
            return new GenerateServiceTicketAction(authenticationSystemSupport, centralAuthenticationService,
                ticketRegistrySupport, authenticationRequestServiceSelectionStrategies,
                servicesManager, principalElectionStrategy);
        }

        @Bean
        @ConditionalOnMissingBean(name = "gatewayServicesManagementCheck")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public Action gatewayServicesManagementCheck(
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
            final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies) {
            return new GatewayServicesManagementCheckAction(servicesManager, authenticationRequestServiceSelectionStrategies);
        }

        @Bean
        @ConditionalOnMissingBean(name = "frontChannelLogoutAction")
        @Autowired
        public Action frontChannelLogoutAction(final CasConfigurationProperties casProperties,
                                               @Qualifier(ServicesManager.BEAN_NAME)
                                               final ServicesManager servicesManager,
                                               @Qualifier("ticketGrantingTicketCookieGenerator")
                                               final CasCookieBuilder ticketGrantingTicketCookieGenerator,
                                               @Qualifier(CentralAuthenticationService.BEAN_NAME)
                                               final CentralAuthenticationService centralAuthenticationService,
                                               @Qualifier("argumentExtractor")
                                               final ArgumentExtractor argumentExtractor,
                                               @Qualifier("logoutExecutionPlan")
                                               final LogoutExecutionPlan logoutExecutionPlan) {
            return new FrontChannelLogoutAction(centralAuthenticationService, ticketGrantingTicketCookieGenerator, argumentExtractor, servicesManager, logoutExecutionPlan, casProperties);
        }

        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_TICKET_GRANTING_TICKET_CHECK)
        @Autowired
        public Action ticketGrantingTicketCheckAction(
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService) {
            return new TicketGrantingTicketCheckAction(centralAuthenticationService);
        }

        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_TERMINATE_SESSION)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public Action terminateSessionAction(final CasConfigurationProperties casProperties,
                                             final ConfigurableApplicationContext applicationContext,
                                             @Qualifier("logoutManager")
                                             final LogoutManager logoutManager,
                                             @Qualifier("ticketGrantingTicketCookieGenerator")
                                             final CasCookieBuilder ticketGrantingTicketCookieGenerator,
                                             @Qualifier("warnCookieGenerator")
                                             final CasCookieBuilder warnCookieGenerator,
                                             @Qualifier(CentralAuthenticationService.BEAN_NAME)
                                             final CentralAuthenticationService centralAuthenticationService,
                                             @Qualifier("defaultSingleLogoutRequestExecutor")
                                             final SingleLogoutRequestExecutor defaultSingleLogoutRequestExecutor) {
            return new TerminateSessionAction(centralAuthenticationService, ticketGrantingTicketCookieGenerator,
                warnCookieGenerator, casProperties.getLogout(), logoutManager,
                applicationContext, defaultSingleLogoutRequestExecutor);
        }

        @Bean
        @ConditionalOnMissingBean(name = "confirmLogoutAction")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public Action confirmLogoutAction(final CasConfigurationProperties casProperties,
                                          @Qualifier(ServicesManager.BEAN_NAME)
                                          final ServicesManager servicesManager,
                                          @Qualifier("ticketGrantingTicketCookieGenerator")
                                          final CasCookieBuilder ticketGrantingTicketCookieGenerator,
                                          @Qualifier(CentralAuthenticationService.BEAN_NAME)
                                          final CentralAuthenticationService centralAuthenticationService,
                                          @Qualifier("argumentExtractor")
                                          final ArgumentExtractor argumentExtractor,
                                          @Qualifier("logoutExecutionPlan")
                                          final LogoutExecutionPlan logoutExecutionPlan) {
            return new ConfirmLogoutAction(centralAuthenticationService, ticketGrantingTicketCookieGenerator,
                argumentExtractor, servicesManager, logoutExecutionPlan, casProperties);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_LOGOUT_VIEW_SETUP)
        @Autowired
        public Action logoutViewSetupAction(final CasConfigurationProperties casProperties,
                                            @Qualifier(ServicesManager.BEAN_NAME)
                                            final ServicesManager servicesManager,
                                            @Qualifier("ticketGrantingTicketCookieGenerator")
                                            final CasCookieBuilder ticketGrantingTicketCookieGenerator,
                                            @Qualifier(CentralAuthenticationService.BEAN_NAME)
                                            final CentralAuthenticationService centralAuthenticationService,
                                            @Qualifier("argumentExtractor")
                                            final ArgumentExtractor argumentExtractor,
                                            @Qualifier("logoutExecutionPlan")
                                            final LogoutExecutionPlan logoutExecutionPlan) {
            return new LogoutViewSetupAction(centralAuthenticationService,
                ticketGrantingTicketCookieGenerator, argumentExtractor, servicesManager, logoutExecutionPlan, casProperties);
        }

        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_SERVICE_WARNING)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action serviceWarningAction(
            @Qualifier("warnCookieGenerator")
            final CasCookieBuilder warnCookieGenerator,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService,
            @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
            final AuthenticationSystemSupport authenticationSystemSupport,
            @Qualifier(TicketRegistrySupport.BEAN_NAME)
            final TicketRegistrySupport ticketRegistrySupport,
            @Qualifier("principalElectionStrategy")
            final PrincipalElectionStrategy principalElectionStrategy) {
            return new ServiceWarningAction(centralAuthenticationService, authenticationSystemSupport,
                ticketRegistrySupport, warnCookieGenerator, principalElectionStrategy);
        }
    }
}
