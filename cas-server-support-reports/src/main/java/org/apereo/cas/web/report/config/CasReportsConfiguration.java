package org.apereo.cas.web.report.config;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.monitor.HealthStatus;
import org.apereo.cas.monitor.Monitor;
import org.apereo.cas.web.report.DashboardController;
import org.apereo.cas.web.report.HealthCheckController;
import org.apereo.cas.web.report.InternalConfigStateController;
import org.apereo.cas.web.report.LoggingConfigController;
import org.apereo.cas.web.report.SingleSignOnSessionsReportController;
import org.apereo.cas.web.report.StatisticsController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    @Qualifier("healthCheckMonitor")
    private Monitor<HealthStatus> healthCheckMonitor;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;


    @Autowired(required = false)
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    @Qualifier("metrics")
    private MetricRegistry metricsRegistry;

    @Autowired
    @Qualifier("healthCheckMetrics")
    private HealthCheckRegistry healthCheckRegistry;

    @RefreshScope
    @Bean
    public DashboardController dashboardController() {
        return new DashboardController();
    }

    @RefreshScope
    @Bean
    public InternalConfigStateController internalConfigController() {
        return new InternalConfigStateController();
    }

    @Bean
    public HealthCheckController healthCheckController() {
        final HealthCheckController c = new HealthCheckController();
        c.setHealthCheckMonitor(healthCheckMonitor);
        return c;
    }

    @Bean
    public SingleSignOnSessionsReportController singleSignOnSessionsReportController() {
        final SingleSignOnSessionsReportController c = new SingleSignOnSessionsReportController();
        c.setCentralAuthenticationService(centralAuthenticationService);
        c.setAuthenticationSystemSupport(authenticationSystemSupport);
        return c;
    }

    @RefreshScope
    @Bean
    public LoggingConfigController loggingConfigController() {
        final LoggingConfigController c = new LoggingConfigController();
        return c;
    }
    
    @Bean
    public StatisticsController statisticsController() {
        final StatisticsController c = new StatisticsController();
        c.setCentralAuthenticationService(centralAuthenticationService);
        c.setHealthCheckRegistry(healthCheckRegistry);
        c.setMetricsRegistry(metricsRegistry);
        return c;
    }

    @Override
    public void configureMessageBroker(final MessageBrokerRegistry config) {
        config.enableSimpleBroker("/logs");
        config.setApplicationDestinationPrefixes("/cas");
    }

    @Override
    public void registerStompEndpoints(final StompEndpointRegistry registry) {
        registry.addEndpoint("/logoutput")
                .addInterceptors(new HttpSessionHandshakeInterceptor())
                .withSockJS();
    }

}
