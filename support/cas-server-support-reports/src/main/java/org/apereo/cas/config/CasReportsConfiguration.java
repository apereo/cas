package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.report.AuditLogEndpoint;
import org.apereo.cas.web.report.CasInfoEndpointContributor;
import org.apereo.cas.web.report.CasReleaseAttributesReportEndpoint;
import org.apereo.cas.web.report.CasResolveAttributesReportEndpoint;
import org.apereo.cas.web.report.ExportRegisteredServicesEndpoint;
import org.apereo.cas.web.report.RegisteredServicesEndpoint;
import org.apereo.cas.web.report.SingleSignOnSessionStatusEndpoint;
import org.apereo.cas.web.report.SingleSignOnSessionsEndpoint;
import org.apereo.cas.web.report.SpringWebflowEndpoint;
import org.apereo.cas.web.report.StatisticsEndpoint;
import org.apereo.cas.web.report.StatusEndpoint;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnEnabledEndpoint;
import org.springframework.boot.actuate.autoconfigure.jdbc.DataSourceHealthIndicatorAutoConfiguration;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * This this {@link CasReportsConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.3.0
 */
@Configuration("casReportsConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasReportsConfiguration {


    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private ObjectProvider<TicketRegistrySupport> ticketRegistrySupport;

    @Autowired
    @Qualifier("ticketGrantingTicketCookieGenerator")
    private ObjectProvider<CookieRetrievingCookieGenerator> ticketGrantingTicketCookieGenerator;

    @Autowired
    @Qualifier("auditTrailExecutionPlan")
    private ObjectProvider<AuditTrailExecutionPlan> auditTrailExecutionPlan;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private ObjectProvider<AuthenticationSystemSupport> authenticationSystemSupport;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ObjectProvider<ServiceFactory<WebApplicationService>> webApplicationServiceFactory;

    @Autowired
    @Qualifier("defaultPrincipalResolver")
    private ObjectProvider<PrincipalResolver> defaultPrincipalResolver;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private ObjectProvider<CentralAuthenticationService> centralAuthenticationService;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("principalFactory")
    private ObjectProvider<PrincipalFactory> principalFactory;

    @Bean
    @ConditionalOnEnabledEndpoint
    public SpringWebflowEndpoint springWebflowEndpoint() {
        return new SpringWebflowEndpoint(casProperties, applicationContext);
    }

    @Bean
    @ConditionalOnEnabledEndpoint
    public AuditLogEndpoint auditLogEndpoint() {
        return new AuditLogEndpoint(auditTrailExecutionPlan.getIfAvailable(), casProperties);
    }

    @Bean
    @ConditionalOnEnabledEndpoint
    public RegisteredServicesEndpoint registeredServicesReportEndpoint() {
        return new RegisteredServicesEndpoint(casProperties, servicesManager.getIfAvailable());
    }

    @Bean
    @ConditionalOnEnabledEndpoint
    public ExportRegisteredServicesEndpoint exportRegisteredServicesEndpoint() {
        return new ExportRegisteredServicesEndpoint(casProperties, servicesManager.getIfAvailable());
    }

    @Bean
    public CasInfoEndpointContributor casInfoEndpointContributor() {
        return new CasInfoEndpointContributor();
    }

    @Bean
    @ConditionalOnEnabledEndpoint
    public SingleSignOnSessionsEndpoint singleSignOnSessionsEndpoint() {
        return new SingleSignOnSessionsEndpoint(centralAuthenticationService.getIfAvailable(), casProperties);
    }

    @Bean
    @ConditionalOnEnabledEndpoint
    public SingleSignOnSessionStatusEndpoint singleSignOnSessionStatusEndpoint() {
        return new SingleSignOnSessionStatusEndpoint(ticketGrantingTicketCookieGenerator.getIfAvailable(), ticketRegistrySupport.getIfAvailable());
    }

    @Bean
    @ConditionalOnEnabledEndpoint
    public StatisticsEndpoint statisticsReportEndpoint() {
        return new StatisticsEndpoint(centralAuthenticationService.getIfAvailable(), casProperties);
    }

    @Bean
    @ConditionalOnEnabledEndpoint
    public CasResolveAttributesReportEndpoint resolveAttributesReportEndpoint() {
        return new CasResolveAttributesReportEndpoint(casProperties, defaultPrincipalResolver.getIfAvailable());
    }

    @Bean
    @ConditionalOnEnabledEndpoint
    public CasReleaseAttributesReportEndpoint releaseAttributesReportEndpoint() {
        return new CasReleaseAttributesReportEndpoint(casProperties,
            servicesManager.getIfAvailable(),
            authenticationSystemSupport.getIfAvailable(),
            webApplicationServiceFactory.getIfAvailable(),
            principalFactory.getIfAvailable());
    }

    /**
     * This this {@link ConditionalDataSourceHealthIndicatorConfiguration}.
     *
     * @author Misagh Moayyed
     * @since 6.0.0
     */
    @ConditionalOnBean(name = "dataSource")
    @Configuration("conditionalDataSourceHealthIndicatorConfiguration")
    @Import(DataSourceHealthIndicatorAutoConfiguration.class)
    public static class ConditionalDataSourceHealthIndicatorConfiguration {
    }

    /**
     * This this {@link StatusEndpointConfiguration}.
     *
     * @author Misagh Moayyed
     * @since 6.0.0
     */
    @Configuration("statusEndpointConfiguration")
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class StatusEndpointConfiguration {
        @Autowired
        private CasConfigurationProperties casProperties;

        @Autowired
        private ObjectProvider<HealthEndpoint> healthEndpoint;

        @Bean
        @ConditionalOnEnabledEndpoint
        public StatusEndpoint statusEndpoint() {
            return new StatusEndpoint(casProperties, healthEndpoint.getIfAvailable());
        }
    }
}
