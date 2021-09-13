package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.slo.SingleLogoutRequestExecutor;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.services.util.RegisteredServiceYamlSerializer;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.feature.CasRuntimeModuleLoader;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.report.AuditLogEndpoint;
import org.apereo.cas.web.report.CasInfoEndpointContributor;
import org.apereo.cas.web.report.CasReleaseAttributesReportEndpoint;
import org.apereo.cas.web.report.CasResolveAttributesReportEndpoint;
import org.apereo.cas.web.report.CasRuntimeModulesEndpoint;
import org.apereo.cas.web.report.RegisteredAuthenticationHandlersEndpoint;
import org.apereo.cas.web.report.RegisteredAuthenticationPoliciesEndpoint;
import org.apereo.cas.web.report.RegisteredServicesEndpoint;
import org.apereo.cas.web.report.SingleSignOnSessionStatusEndpoint;
import org.apereo.cas.web.report.SingleSignOnSessionsEndpoint;
import org.apereo.cas.web.report.SpringWebflowEndpoint;
import org.apereo.cas.web.report.StatisticsEndpoint;
import org.apereo.cas.web.report.StatusEndpoint;
import org.apereo.cas.web.report.TicketExpirationPoliciesEndpoint;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.trace.http.HttpTraceEndpoint;
import org.springframework.boot.actuate.trace.http.HttpTraceRepository;
import org.springframework.boot.actuate.trace.http.InMemoryHttpTraceRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

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
    @Qualifier("defaultSingleLogoutRequestExecutor")
    private ObjectProvider<SingleLogoutRequestExecutor> defaultSingleLogoutRequestExecutor;

    @Autowired
    @Qualifier("casRuntimeModuleLoader")
    private ObjectProvider<CasRuntimeModuleLoader> casRuntimeModuleLoader;

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
    @Qualifier(AuthenticationEventExecutionPlan.DEFAULT_BEAN_NAME)
    private ObjectProvider<AuthenticationEventExecutionPlan> authenticationEventExecutionPlan;

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
    public CasRuntimeModulesEndpoint casRuntimeModulesEndpoint() {
        return new CasRuntimeModulesEndpoint(casProperties, casRuntimeModuleLoader.getObject());
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    public RegisteredServicesEndpoint registeredServicesReportEndpoint() {
        return new RegisteredServicesEndpoint(casProperties, servicesManager.getObject(),
            webApplicationServiceFactory.getObject(),
            CollectionUtils.wrapList(new RegisteredServiceYamlSerializer(), new RegisteredServiceJsonSerializer()));
    }


    @Bean
    @ConditionalOnAvailableEndpoint
    public RegisteredAuthenticationHandlersEndpoint registeredAuthenticationHandlersEndpoint() {
        return new RegisteredAuthenticationHandlersEndpoint(casProperties, authenticationEventExecutionPlan.getObject());
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    public RegisteredAuthenticationPoliciesEndpoint registeredAuthenticationPoliciesEndpoint() {
        return new RegisteredAuthenticationPoliciesEndpoint(casProperties, authenticationEventExecutionPlan.getObject());
    }

    @Bean
    @ConditionalOnMissingBean(name = "casInfoEndpointContributor")
    public CasInfoEndpointContributor casInfoEndpointContributor() {
        return new CasInfoEndpointContributor(casRuntimeModuleLoader.getObject());
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    public SingleSignOnSessionsEndpoint singleSignOnSessionsEndpoint() {
        return new SingleSignOnSessionsEndpoint(centralAuthenticationService.getObject(),
            casProperties, defaultSingleLogoutRequestExecutor.getObject());
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

    @Autowired
    @Bean
    @ConditionalOnAvailableEndpoint
    public TicketExpirationPoliciesEndpoint ticketExpirationPoliciesEndpoint(final List<ExpirationPolicyBuilder> builders) {
        return new TicketExpirationPoliciesEndpoint(casProperties, builders, servicesManager.getObject(), webApplicationServiceFactory.getObject());
    }

    @Bean
    @ConditionalOnAvailableEndpoint(endpoint = HttpTraceEndpoint.class)
    public HttpTraceRepository httpTraceRepository() {
        return new InMemoryHttpTraceRepository();
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

    /**
     * This this {@link StatusEndpointConfiguration}.
     *
     * @author Misagh Moayyed
     * @since 6.0.0
     * @deprecated since 6.2.0
     */
    @Configuration("statusEndpointConfiguration")
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @Slf4j
    @Deprecated(since = "6.2.0")
    public static class StatusEndpointConfiguration {
        @Autowired
        private CasConfigurationProperties casProperties;

        @Autowired
        private ObjectProvider<HealthEndpoint> healthEndpoint;

        @Bean
        @ConditionalOnAvailableEndpoint
        public StatusEndpoint statusEndpoint() {
            LOGGER.warn("The status actuator endpoint is deprecated and is scheduled to be removed from CAS in the future. "
                + "To obtain status and health inforation, please configure and use the health endpoint instead.");
            return new StatusEndpoint(casProperties, healthEndpoint.getIfAvailable());
        }
    }
}
