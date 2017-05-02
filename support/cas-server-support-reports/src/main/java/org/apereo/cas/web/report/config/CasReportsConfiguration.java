package org.apereo.cas.web.report.config;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.spi.DelegatingAuditTrailManager;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.monitor.HealthStatus;
import org.apereo.cas.monitor.Monitor;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.web.report.AuthenticationEventsController;
import org.apereo.cas.web.report.ConfigurationStateController;
import org.apereo.cas.web.report.DashboardController;
import org.apereo.cas.web.report.HealthCheckController;
import org.apereo.cas.web.report.LoggingConfigController;
import org.apereo.cas.web.report.LoggingOutputSocketMessagingController;
import org.apereo.cas.web.report.MetricsController;
import org.apereo.cas.web.report.PersonDirectoryAttributeResolutionController;
import org.apereo.cas.web.report.SingleSignOnSessionStatusController;
import org.apereo.cas.web.report.SingleSignOnSessionsReportController;
import org.apereo.cas.web.report.SpringWebflowReportController;
import org.apereo.cas.web.report.StatisticsController;
import org.apereo.cas.web.report.TrustedDevicesController;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.endpoint.mvc.MvcEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
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
public class CasReportsConfiguration extends AbstractWebSocketMessageBrokerConfigurer {

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private TicketRegistrySupport ticketRegistrySupport;

    @Autowired
    @Qualifier("ticketGrantingTicketCookieGenerator")
    private CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ServerProperties serverProperties;

    @Autowired
    @Qualifier("healthCheckMonitor")
    private Monitor<HealthStatus> healthCheckMonitor;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired
    @Qualifier("metrics")
    private MetricRegistry metricsRegistry;

    @Autowired
    @Qualifier("healthCheckMetrics")
    private HealthCheckRegistry healthCheckRegistry;

    @Bean
    @RefreshScope
    public MvcEndpoint dashboardController() {
        return new DashboardController(casProperties);
    }

    @Bean
    @RefreshScope
    public MvcEndpoint personDirectoryAttributeResolutionController() {
        return new PersonDirectoryAttributeResolutionController(casProperties);
    }

    @Profile("standalone")
    @ConditionalOnBean(name = "configurationPropertiesEnvironmentManager")
    @Bean
    @RefreshScope
    public MvcEndpoint internalConfigController() {
        return new ConfigurationStateController(casProperties);
    }

    @Bean
    @RefreshScope
    public MvcEndpoint healthCheckController() {
        return new HealthCheckController(healthCheckMonitor, casProperties);
    }

    @Bean
    @RefreshScope
    public MvcEndpoint singleSignOnSessionsReportController() {
        return new SingleSignOnSessionsReportController(centralAuthenticationService, casProperties);
    }

    @Bean
    @RefreshScope
    @Autowired
    public MvcEndpoint loggingConfigController(@Qualifier("auditTrailManager") final DelegatingAuditTrailManager auditTrailManager) {
        return new LoggingConfigController(auditTrailManager, casProperties);
    }

    @Bean
    @RefreshScope
    public MvcEndpoint ssoStatusController() {
        return new SingleSignOnSessionStatusController(ticketGrantingTicketCookieGenerator, ticketRegistrySupport, casProperties);
    }

    @Bean
    @RefreshScope
    public MvcEndpoint swfReportController() {
        return new SpringWebflowReportController(casProperties);
    }

    @Autowired
    @Bean
    @RefreshScope
    public MvcEndpoint statisticsController(@Qualifier("auditTrailManager") final DelegatingAuditTrailManager auditTrailManager) {
        return new StatisticsController(auditTrailManager, centralAuthenticationService,
                metricsRegistry, healthCheckRegistry, casProperties);
    }

    @Bean
    @RefreshScope
    public MvcEndpoint metricsController() {
        return new MetricsController(casProperties);
    }

    @Bean
    @RefreshScope
    public LoggingOutputSocketMessagingController loggingOutputController() {
        return new LoggingOutputSocketMessagingController();
    }

    @Override
    public void configureMessageBroker(final MessageBrokerRegistry config) {
        config.enableSimpleBroker("/logs");
        if (StringUtils.isNotBlank(serverProperties.getContextPath())) {
            config.setApplicationDestinationPrefixes(serverProperties.getContextPath());
        }
    }

    @Override
    public void registerStompEndpoints(final StompEndpointRegistry registry) {
        registry.addEndpoint("/logoutput")
                .addInterceptors(new HttpSessionHandshakeInterceptor())
                .withSockJS();
    }

    /**
     * The Trusted devices configuration for the UI.
     */
    @ConditionalOnClass(value = MultifactorAuthenticationTrustStorage.class)
    @Configuration("trustedDevicesConfiguration")
    public class TrustedDevicesConfiguration {

        @Autowired
        @Bean
        public MvcEndpoint trustedDevicesController(@Qualifier("mfaTrustEngine") final MultifactorAuthenticationTrustStorage mfaTrustEngine) {
            return new TrustedDevicesController(mfaTrustEngine, casProperties);
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
        public MvcEndpoint authenticationEventsController(@Qualifier("casEventRepository") final CasEventRepository eventRepository) {
            return new AuthenticationEventsController(eventRepository, casProperties);
        }
    }
}
