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
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.report.AuditLogEndpoint;
import org.apereo.cas.web.report.CasApplicationContextReloadEndpoint;
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

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This this {@link CasReportsConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.3.0
 */
@Configuration(value = "casReportsConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasReportsConfiguration {
    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private ObjectProvider<TicketRegistrySupport> ticketRegistrySupport;

    @Autowired
    @Qualifier("ticketGrantingTicketCookieGenerator")
    private ObjectProvider<CasCookieBuilder> ticketGrantingTicketCookieGenerator;

    @Autowired
    @Qualifier("auditTrailExecutionPlan")
    private ObjectProvider<AuditTrailExecutionPlan> auditTrailExecutionPlan;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

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

    @Autowired
    private ObjectProvider<ContextRefresher> contextRefresher;

    @Bean
    @ConditionalOnAvailableEndpoint
    public SpringWebflowEndpoint springWebflowEndpoint() {
        return new SpringWebflowEndpoint(casProperties, applicationContext);
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    public AuditLogEndpoint auditLogEndpoint() {
        return new AuditLogEndpoint(auditTrailExecutionPlan.getObject(), casProperties);
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    public RegisteredServicesEndpoint registeredServicesReportEndpoint() {
        return new RegisteredServicesEndpoint(casProperties, servicesManager.getObject());
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    public ExportRegisteredServicesEndpoint exportRegisteredServicesEndpoint() {
        return new ExportRegisteredServicesEndpoint(casProperties, servicesManager.getObject());
    }

    @Bean
    public CasInfoEndpointContributor casInfoEndpointContributor() {
        return new CasInfoEndpointContributor();
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    public SingleSignOnSessionsEndpoint singleSignOnSessionsEndpoint() {
        return new SingleSignOnSessionsEndpoint(centralAuthenticationService.getObject(), casProperties);
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    public SingleSignOnSessionStatusEndpoint singleSignOnSessionStatusEndpoint() {
        return new SingleSignOnSessionStatusEndpoint(ticketGrantingTicketCookieGenerator.getObject(), ticketRegistrySupport.getObject());
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    public StatisticsEndpoint statisticsReportEndpoint() {
        return new StatisticsEndpoint(centralAuthenticationService.getObject(), casProperties);
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    public CasResolveAttributesReportEndpoint resolveAttributesReportEndpoint() {
        return new CasResolveAttributesReportEndpoint(casProperties, defaultPrincipalResolver.getObject());
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    public CasReleaseAttributesReportEndpoint releaseAttributesReportEndpoint() {
        return new CasReleaseAttributesReportEndpoint(casProperties,
            servicesManager.getObject(),
            authenticationSystemSupport.getObject(),
            webApplicationServiceFactory.getObject(),
            principalFactory.getObject());
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    public CasApplicationContextReloadEndpoint casApplicationContextReloadEndpoint() {
        return new CasApplicationContextReloadEndpoint(casProperties, contextRefresher.getObject());
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
        @ConditionalOnAvailableEndpoint
        public StatusEndpoint statusEndpoint() {
            return new StatusEndpoint(casProperties, healthEndpoint.getIfAvailable());
        }
    }
}
