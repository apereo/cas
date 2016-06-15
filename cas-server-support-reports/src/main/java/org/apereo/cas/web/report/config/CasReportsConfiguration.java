package org.apereo.cas.web.report.config;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.monitor.HealthStatus;
import org.apereo.cas.monitor.Monitor;
import org.apereo.cas.web.report.DashboardController;
import org.apereo.cas.web.report.HealthCheckController;
import org.apereo.cas.web.report.InternalConfigStateController;
import org.apereo.cas.web.report.SingleSignOnSessionsReportController;
import org.apereo.cas.web.report.StatisticsController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * This is {@link CasReportsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casReportsConfiguration")
public class CasReportsConfiguration {

    @Resource(name = "healthCheckMonitor")
    private Monitor<HealthStatus> healthCheckMonitor;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;


    @Autowired(required = false)
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Resource(name = "metrics")
    private MetricRegistry metricsRegistry;

    @Resource(name = "healthCheckMetrics")
    private HealthCheckRegistry healthCheckRegistry;

    @Bean
    public DashboardController dashboardController() {
        return new DashboardController();
    }

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

    @Bean
    public StatisticsController statisticsController() {
        final StatisticsController c = new StatisticsController();
        c.setCentralAuthenticationService(centralAuthenticationService);
        c.setHealthCheckRegistry(healthCheckRegistry);
        c.setMetricsRegistry(metricsRegistry);
        return c;
    }


}
