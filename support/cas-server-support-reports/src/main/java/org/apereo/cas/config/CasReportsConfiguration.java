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
import org.springframework.beans.factory.InitializingBean;
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

    @Bean
    @Autowired
    @ConditionalOnAvailableEndpoint
    public SpringWebflowEndpoint springWebflowEndpoint(final CasConfigurationProperties casProperties,
                                                       final ConfigurableApplicationContext applicationContext) {
        return new SpringWebflowEndpoint(casProperties, applicationContext);
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    @Autowired
    public AuditLogEndpoint auditLogEndpoint(
        @Qualifier("auditTrailExecutionPlan")
        final AuditTrailExecutionPlan auditTrailExecutionPlan,
        final CasConfigurationProperties casProperties) {
        return new AuditLogEndpoint(auditTrailExecutionPlan, casProperties);
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    @Autowired
    public CasRuntimeModulesEndpoint casRuntimeModulesEndpoint(
        @Qualifier("casRuntimeModuleLoader")
        final CasRuntimeModuleLoader casRuntimeModuleLoader,
        final CasConfigurationProperties casProperties) {
        return new CasRuntimeModulesEndpoint(casProperties, casRuntimeModuleLoader);
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    @Autowired
    public RegisteredServicesEndpoint registeredServicesReportEndpoint(
        @Qualifier("webApplicationServiceFactory")
        final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        final CasConfigurationProperties casProperties) {
        return new RegisteredServicesEndpoint(casProperties, servicesManager,
            webApplicationServiceFactory,
            CollectionUtils.wrapList(new RegisteredServiceYamlSerializer(), new RegisteredServiceJsonSerializer()));
    }


    @Bean
    @ConditionalOnAvailableEndpoint
    @Autowired
    public RegisteredAuthenticationHandlersEndpoint registeredAuthenticationHandlersEndpoint(
        @Qualifier(AuthenticationEventExecutionPlan.DEFAULT_BEAN_NAME)
        final AuthenticationEventExecutionPlan authenticationEventExecutionPlan,
        final CasConfigurationProperties casProperties) {
        return new RegisteredAuthenticationHandlersEndpoint(casProperties, authenticationEventExecutionPlan);
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    @Autowired
    public RegisteredAuthenticationPoliciesEndpoint registeredAuthenticationPoliciesEndpoint(
        @Qualifier(AuthenticationEventExecutionPlan.DEFAULT_BEAN_NAME)
        final AuthenticationEventExecutionPlan authenticationEventExecutionPlan,
        final CasConfigurationProperties casProperties) {
        return new RegisteredAuthenticationPoliciesEndpoint(casProperties, authenticationEventExecutionPlan);
    }

    @Bean
    @Autowired
    @ConditionalOnMissingBean(name = "casInfoEndpointContributor")
    public CasInfoEndpointContributor casInfoEndpointContributor(
        @Qualifier("casRuntimeModuleLoader")
        final CasRuntimeModuleLoader casRuntimeModuleLoader) {
        return new CasInfoEndpointContributor(casRuntimeModuleLoader);
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    @Autowired
    public SingleSignOnSessionsEndpoint singleSignOnSessionsEndpoint(
        @Qualifier(CentralAuthenticationService.BEAN_NAME)
        final CentralAuthenticationService centralAuthenticationService,
        @Qualifier("defaultSingleLogoutRequestExecutor")
        final SingleLogoutRequestExecutor defaultSingleLogoutRequestExecutor,
        final CasConfigurationProperties casProperties) {
        return new SingleSignOnSessionsEndpoint(centralAuthenticationService,
            casProperties, defaultSingleLogoutRequestExecutor);
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    @Autowired
    public SingleSignOnSessionStatusEndpoint singleSignOnSessionStatusEndpoint(
        @Qualifier("ticketGrantingTicketCookieGenerator")
        final CasCookieBuilder ticketGrantingTicketCookieGenerator,
        @Qualifier(TicketRegistrySupport.BEAN_NAME)
        final TicketRegistrySupport ticketRegistrySupport) {
        return new SingleSignOnSessionStatusEndpoint(ticketGrantingTicketCookieGenerator, ticketRegistrySupport);
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    @Autowired
    public StatisticsEndpoint statisticsReportEndpoint(
        @Qualifier(CentralAuthenticationService.BEAN_NAME)
        final CentralAuthenticationService centralAuthenticationService,
        final CasConfigurationProperties casProperties) {
        return new StatisticsEndpoint(centralAuthenticationService, casProperties);
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    @Autowired
    public CasResolveAttributesReportEndpoint resolveAttributesReportEndpoint(
        @Qualifier("defaultPrincipalResolver")
        final PrincipalResolver defaultPrincipalResolver,
        final CasConfigurationProperties casProperties) {
        return new CasResolveAttributesReportEndpoint(casProperties, defaultPrincipalResolver);
    }

    @Autowired
    @Bean
    @ConditionalOnAvailableEndpoint
    public TicketExpirationPoliciesEndpoint ticketExpirationPoliciesEndpoint(
        @Qualifier("webApplicationServiceFactory")
        final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        final CasConfigurationProperties casProperties,
        final List<ExpirationPolicyBuilder> builders) {
        return new TicketExpirationPoliciesEndpoint(casProperties, builders,
            servicesManager, webApplicationServiceFactory);
    }

    @Bean
    @ConditionalOnAvailableEndpoint(endpoint = HttpTraceEndpoint.class)
    public HttpTraceRepository httpTraceRepository() {
        return new InMemoryHttpTraceRepository();
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    @Autowired
    public CasReleaseAttributesReportEndpoint releaseAttributesReportEndpoint(
        @Qualifier("webApplicationServiceFactory")
        final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
        @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
        final AuthenticationSystemSupport authenticationSystemSupport,
        @Qualifier("principalFactory")
        final PrincipalFactory principalFactory,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        final CasConfigurationProperties casProperties) {
        return new CasReleaseAttributesReportEndpoint(casProperties,
            servicesManager, authenticationSystemSupport,
            webApplicationServiceFactory, principalFactory);
    }

    /**
     * This this {@link StatusEndpointConfiguration}.
     *
     * @author Misagh Moayyed
     * @since 6.0.0
     * @deprecated since 6.2.0
     */
    @Configuration(value = "statusEndpointConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @Slf4j
    @Deprecated(since = "6.2.0")
    public static class StatusEndpointConfiguration {
        @Bean
        @ConditionalOnAvailableEndpoint
        @Autowired
        public StatusEndpoint statusEndpoint(
            final ObjectProvider<HealthEndpoint> healthEndpoint,
            final CasConfigurationProperties casProperties) {
            return new StatusEndpoint(casProperties, healthEndpoint.getIfAvailable());
        }

        @Bean
        @ConditionalOnAvailableEndpoint(endpoint = StatusEndpoint.class)
        public InitializingBean statusEndpointInitializer() {
            return () ->
                LOGGER.warn("The status actuator endpoint is deprecated and is scheduled to be removed from CAS in the future. "
                    + "To obtain status and health information, please configure and use the health endpoint instead.");
        }
    }
}
