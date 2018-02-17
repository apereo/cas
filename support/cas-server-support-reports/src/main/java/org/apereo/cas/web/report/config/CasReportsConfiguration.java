package org.apereo.cas.web.report.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.discovery.CasServerProfileRegistrar;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.web.report.AuthenticationEventsEndpoint;
import org.apereo.cas.web.report.CasInfoEndpointContributor;
import org.apereo.cas.web.report.CasServerDiscoveryProfileEndpoint;
import org.apereo.cas.web.report.ConfigurationStateEndpoint;
import org.apereo.cas.web.report.DashboardEndpoint;
import org.apereo.cas.web.report.LoggingConfigurationEndpoint;
import org.apereo.cas.web.report.LoggingOutputTailingService;
import org.apereo.cas.web.report.MultifactorAuthenticationTrustedDevicesEndpoint;
import org.apereo.cas.web.report.PersonDirectoryEndpoint;
import org.apereo.cas.web.report.RegisteredServicesEndpoint;
import org.apereo.cas.web.report.SingleSignOnSessionStatusEndpoint;
import org.apereo.cas.web.report.SingleSignOnSessionsEndpoint;
import org.apereo.cas.web.report.SpringWebflowEndpoint;
import org.apereo.cas.web.report.StatisticsEndpoint;
import org.apereo.cas.web.report.StatusEndpoint;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnEnabledEndpoint;
import org.springframework.boot.actuate.endpoint.mvc.MvcEndpoint;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.TaskExecutor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.View;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

/**
 * This is {@link CasReportsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casReportsConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableWebSocketMessageBroker
@Slf4j
public class CasReportsConfiguration extends AbstractWebSocketMessageBrokerConfigurer {
    private static final int LOG_TAILING_CORE_POOL_SIZE = 5;
    private static final int LOG_TAILING_QUEUE_CAPACITY = 25;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    @Qualifier("personDirectoryPrincipalResolver")
    private PrincipalResolver personDirectoryPrincipalResolver;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    @Autowired
    @Qualifier("cas3ServiceSuccessView")
    private View cas3ServiceSuccessView;

    @Autowired
    @Qualifier("cas3ServiceJsonView")
    private View cas3ServiceJsonView;

    @Autowired
    @Qualifier("cas2ServiceSuccessView")
    private View cas2ServiceSuccessView;

    @Autowired
    @Qualifier("cas1ServiceSuccessView")
    private View cas1ServiceSuccessView;

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private TicketRegistrySupport ticketRegistrySupport;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("principalFactory")
    private PrincipalFactory principalFactory;

    @Autowired
    @Qualifier("ticketGrantingTicketCookieGenerator")
    private CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ServerProperties serverProperties;

    @Autowired
    private HealthEndpoint healthEndpoint;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;

    @Bean
    @ConditionalOnEnabledEndpoint
    public DashboardEndpoint dashboardEndpoint() {
        return new DashboardEndpoint(casProperties);
    }

    @Bean
    @ConditionalOnEnabledEndpoint
    public PersonDirectoryEndpoint personDirectoryEndpoint() {
        return new PersonDirectoryEndpoint(casProperties, servicesManager,
            authenticationSystemSupport, personDirectoryPrincipalResolver, webApplicationServiceFactory,
            principalFactory, cas3ServiceSuccessView, cas3ServiceJsonView, cas2ServiceSuccessView, cas1ServiceSuccessView);
    }

    @Profile("standalone")
    @ConditionalOnBean(name = "configurationPropertiesEnvironmentManager")
    @Bean
    @ConditionalOnEnabledEndpoint
    public ConfigurationStateEndpoint internalConfigEndpoint() {
        return new ConfigurationStateEndpoint(casProperties);
    }

    @Bean
    @ConditionalOnEnabledEndpoint
    public StatusEndpoint statusEndpoint() {
        return new StatusEndpoint(casProperties, healthEndpoint);
    }

    @Bean
    @ConditionalOnEnabledEndpoint
    public SingleSignOnSessionsEndpoint singleSignOnSessionsEndpoint() {
        return new SingleSignOnSessionsEndpoint(centralAuthenticationService, casProperties);
    }

    @Bean
    @ConditionalOnEnabledEndpoint
    public RegisteredServicesEndpoint registeredServicesEndpoint() {
        return new RegisteredServicesEndpoint(casProperties, servicesManager);
    }

    @Bean
    @Autowired
    @ConditionalOnEnabledEndpoint
    public LoggingConfigurationEndpoint loggingConfigurationEndpoint(@Qualifier("auditTrailExecutionPlan") final AuditTrailExecutionPlan auditTrailManager) {
        return new LoggingConfigurationEndpoint(auditTrailManager, casProperties);
    }

    @Bean
    @ConditionalOnEnabledEndpoint
    public SingleSignOnSessionStatusEndpoint ssoSessionStatusEndpoint() {
        return new SingleSignOnSessionStatusEndpoint(ticketGrantingTicketCookieGenerator, ticketRegistrySupport, casProperties);
    }

    @Bean
    @ConditionalOnEnabledEndpoint
    public SpringWebflowEndpoint springWebflowEndpoint() {
        return new SpringWebflowEndpoint(casProperties, applicationContext);
    }

    @Autowired
    @Bean
    @ConditionalOnEnabledEndpoint
    public StatisticsEndpoint statisticsEndpoint(@Qualifier("auditTrailExecutionPlan") final AuditTrailExecutionPlan auditTrailManager) {
        return new StatisticsEndpoint(auditTrailManager, centralAuthenticationService, casProperties);
    }

    @Bean
    public InfoContributor casInfoEndpointContributor() {
        return new CasInfoEndpointContributor();
    }

    @Bean
    public TaskExecutor logTailingTaskExecutor() {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(LOG_TAILING_CORE_POOL_SIZE);
        executor.setQueueCapacity(LOG_TAILING_QUEUE_CAPACITY);
        return executor;
    }

    @Bean
    public LoggingOutputTailingService loggingOutputTailingService(final SimpMessagingTemplate simpMessagingTemplate) {
        return new LoggingOutputTailingService(logTailingTaskExecutor(), simpMessagingTemplate);
    }

    /**
     * The type Trusted devices configuration.
     */
    @ConditionalOnClass(value = MultifactorAuthenticationTrustStorage.class)
    @Configuration("trustedDevicesConfiguration")
    public class TrustedDevicesConfiguration {

        @Autowired
        @Bean
        @ConditionalOnClass(value = MultifactorAuthenticationTrustStorage.class)
        @ConditionalOnEnabledEndpoint
        public MultifactorAuthenticationTrustedDevicesEndpoint multifactorAuthenticationTrustedDevicesEndpoint(@Qualifier("mfaTrustEngine") final MultifactorAuthenticationTrustStorage mfaTrustEngine) {
            return new MultifactorAuthenticationTrustedDevicesEndpoint(mfaTrustEngine, casProperties);
        }
    }

    /**
     * The type Authentication events configuration.
     */
    @ConditionalOnClass(value = CasEventRepository.class)
    @Configuration("authenticationEventsConfiguration")
    public class AuthenticationEventsConfiguration {

        @Autowired
        @Bean
        @ConditionalOnClass(value = CasEventRepository.class)
        @ConditionalOnEnabledEndpoint
        @ConditionalOnMissingBean(name = "authenticationEventsEndpoint")
        public AuthenticationEventsEndpoint authenticationEventsEndpoint(@Qualifier("casEventRepository") final CasEventRepository eventRepository) {
            return new AuthenticationEventsEndpoint(eventRepository, casProperties);
        }
    }

    /**
     * The type server discovery profile configuration.
     */
    @ConditionalOnClass(value = CasServerProfileRegistrar.class)
    @Configuration("serverDiscoveryProfileConfiguration")
    public class ServerDiscoveryProfileConfiguration {

        @Autowired
        @Bean
        @ConditionalOnEnabledEndpoint
        public CasServerDiscoveryProfileEndpoint discoveryProfileEndpoint(@Qualifier("casServerProfileRegistrar") final CasServerProfileRegistrar casServerProfileRegistrar) {
            return new CasServerDiscoveryProfileEndpoint(casProperties, servicesManager, casServerProfileRegistrar);
        }
    }

    @Override
    public void configureMessageBroker(final MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        final String contextPath = serverProperties.getServlet().getContextPath();
        if (StringUtils.isNotBlank(contextPath)) {
            config.setApplicationDestinationPrefixes(contextPath);
        }
    }

    @Override
    public void registerStompEndpoints(final StompEndpointRegistry registry) {
        registry.addEndpoint("/reports-websocket")
            .addInterceptors(new HttpSessionHandshakeInterceptor())
            .withSockJS();
    }
}
