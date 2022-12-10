package org.apereo.cas.config;

import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.logout.slo.SingleLogoutRequestExecutor;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.services.util.RegisteredServiceYamlSerializer;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.feature.CasRuntimeModuleLoader;
import org.apereo.cas.util.spring.DirectObjectProvider;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.report.AuditLogEndpoint;
import org.apereo.cas.web.report.CasFeaturesEndpoint;
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
import org.apereo.cas.web.report.TicketExpirationPoliciesEndpoint;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.web.exchanges.HttpExchangeRepository;
import org.springframework.boot.actuate.web.exchanges.HttpExchangesEndpoint;
import org.springframework.boot.actuate.web.exchanges.InMemoryHttpExchangeRepository;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

import java.util.List;

/**
 * This this {@link CasReportsConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.3.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Reports)
@AutoConfiguration
public class CasReportsConfiguration {

    @Configuration(value = "AttributesEndpointsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class AttributesEndpointsConfiguration {
        @Bean
        @ConditionalOnAvailableEndpoint
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasResolveAttributesReportEndpoint resolveAttributesReportEndpoint(
            @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
            final ObjectProvider<PrincipalResolver> defaultPrincipalResolver,
            final CasConfigurationProperties casProperties) {
            return new CasResolveAttributesReportEndpoint(casProperties, defaultPrincipalResolver);
        }

        @Bean
        @ConditionalOnAvailableEndpoint
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasReleaseAttributesReportEndpoint releaseAttributesReportEndpoint(
            @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
            final ObjectProvider<ServiceFactory<WebApplicationService>> webApplicationServiceFactory,
            @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
            final ObjectProvider<AuthenticationSystemSupport> authenticationSystemSupport,
            @Qualifier("principalFactory")
            final ObjectProvider<PrincipalFactory> principalFactory,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ObjectProvider<ServicesManager> servicesManager,
            final CasConfigurationProperties casProperties) {
            return new CasReleaseAttributesReportEndpoint(casProperties,
                servicesManager, authenticationSystemSupport,
                webApplicationServiceFactory, principalFactory);
        }
    }

    @Configuration(value = "SingleSignOnEndpointsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SingleSignOnEndpointsConfiguration {
        @Bean
        @ConditionalOnAvailableEndpoint
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SingleSignOnSessionsEndpoint singleSignOnSessionsEndpoint(
            @Qualifier(TicketRegistry.BEAN_NAME)
            final ObjectProvider<TicketRegistry> ticketRegistry,
            @Qualifier(SingleLogoutRequestExecutor.BEAN_NAME)
            final ObjectProvider<SingleLogoutRequestExecutor> defaultSingleLogoutRequestExecutor,
            final CasConfigurationProperties casProperties) {
            return new SingleSignOnSessionsEndpoint(ticketRegistry,
                casProperties, defaultSingleLogoutRequestExecutor);
        }

        @Bean
        @ConditionalOnAvailableEndpoint
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SingleSignOnSessionStatusEndpoint singleSignOnSessionStatusEndpoint(
            @Qualifier(CasCookieBuilder.BEAN_NAME_TICKET_GRANTING_COOKIE_BUILDER)
            final ObjectProvider<CasCookieBuilder> ticketGrantingTicketCookieGenerator,
            @Qualifier(TicketRegistrySupport.BEAN_NAME)
            final ObjectProvider<TicketRegistrySupport> ticketRegistrySupport) {
            return new SingleSignOnSessionStatusEndpoint(ticketGrantingTicketCookieGenerator, ticketRegistrySupport);
        }
    }

    @Configuration(value = "AuthenticationEndpointsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class AuthenticationEndpointsConfiguration {
        @Bean
        @ConditionalOnAvailableEndpoint
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public RegisteredAuthenticationHandlersEndpoint registeredAuthenticationHandlersEndpoint(
            @Qualifier(AuthenticationEventExecutionPlan.DEFAULT_BEAN_NAME)
            final ObjectProvider<AuthenticationEventExecutionPlan> authenticationEventExecutionPlan,
            final CasConfigurationProperties casProperties) {
            return new RegisteredAuthenticationHandlersEndpoint(casProperties, authenticationEventExecutionPlan);
        }

        @Bean
        @ConditionalOnAvailableEndpoint
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public RegisteredAuthenticationPoliciesEndpoint registeredAuthenticationPoliciesEndpoint(
            @Qualifier(AuthenticationEventExecutionPlan.DEFAULT_BEAN_NAME)
            final ObjectProvider<AuthenticationEventExecutionPlan> authenticationEventExecutionPlan,
            final CasConfigurationProperties casProperties) {
            return new RegisteredAuthenticationPoliciesEndpoint(casProperties, authenticationEventExecutionPlan);
        }

    }

    @Configuration(value = "SystemInfoEndpointsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SystemInfoEndpointsConfiguration {
        @Bean
        @ConditionalOnAvailableEndpoint
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasFeaturesEndpoint casFeaturesEndpoint(final CasConfigurationProperties casProperties) {
            return new CasFeaturesEndpoint(casProperties);
        }

        @Bean
        @ConditionalOnAvailableEndpoint
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SpringWebflowEndpoint springWebflowEndpoint(final CasConfigurationProperties casProperties,
                                                           final ConfigurableApplicationContext applicationContext) {
            return new SpringWebflowEndpoint(casProperties, applicationContext);
        }

        @Bean
        @ConditionalOnAvailableEndpoint
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasRuntimeModulesEndpoint casRuntimeModulesEndpoint(
            @Qualifier("casRuntimeModuleLoader")
            final ObjectProvider<CasRuntimeModuleLoader> casRuntimeModuleLoader,
            final CasConfigurationProperties casProperties) {
            return new CasRuntimeModulesEndpoint(casProperties, casRuntimeModuleLoader);
        }


        @Bean
        @ConditionalOnMissingBean(name = "casInfoEndpointContributor")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasInfoEndpointContributor casInfoEndpointContributor(
            @Qualifier("casRuntimeModuleLoader")
            final CasRuntimeModuleLoader casRuntimeModuleLoader) {
            return new CasInfoEndpointContributor(casRuntimeModuleLoader);
        }
    }

    @Configuration(value = "RegisteredServicesEndpointsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class RegisteredServicesEndpointsConfiguration {
        @Bean
        @ConditionalOnAvailableEndpoint
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public RegisteredServicesEndpoint registeredServicesReportEndpoint(
            final ObjectProvider<ConfigurableApplicationContext> applicationContext,
            @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
            final ObjectProvider<ServiceFactory<WebApplicationService>> webApplicationServiceFactory,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ObjectProvider<ServicesManager> servicesManager,
            final CasConfigurationProperties casProperties) {

            val serializers = CollectionUtils.wrapList(
                new RegisteredServiceYamlSerializer(applicationContext.getObject()),
                new RegisteredServiceJsonSerializer(applicationContext.getObject()));
            return new RegisteredServicesEndpoint(casProperties,
                servicesManager,
                webApplicationServiceFactory,
                new DirectObjectProvider<>(serializers),
                applicationContext);
        }

    }

    @Configuration(value = "AuditActivityEndpointsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class AuditActivityEndpointsConfiguration {
        @Bean
        @ConditionalOnAvailableEndpoint
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditLogEndpoint auditLogEndpoint(
            @Qualifier(AuditTrailExecutionPlan.BEAN_NAME)
            final ObjectProvider<AuditTrailExecutionPlan> auditTrailExecutionPlan,
            final CasConfigurationProperties casProperties) {
            return new AuditLogEndpoint(auditTrailExecutionPlan, casProperties);
        }
    }

    @Configuration(value = "TicketingEndpointsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class TicketingEndpointsConfiguration {
        @Bean
        @ConditionalOnAvailableEndpoint
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public StatisticsEndpoint statisticsReportEndpoint(
            @Qualifier(TicketRegistry.BEAN_NAME)
            final ObjectProvider<TicketRegistry> ticketRegistry,
            final CasConfigurationProperties casProperties) {
            return new StatisticsEndpoint(ticketRegistry, casProperties);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnAvailableEndpoint
        public TicketExpirationPoliciesEndpoint ticketExpirationPoliciesEndpoint(
            @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ObjectProvider<ServicesManager> servicesManager,
            final CasConfigurationProperties casProperties,
            final List<ExpirationPolicyBuilder> builders) {
            return new TicketExpirationPoliciesEndpoint(casProperties, builders,
                servicesManager, webApplicationServiceFactory);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnAvailableEndpoint(endpoint = HttpExchangesEndpoint.class)
        public HttpExchangeRepository exchangeRepository() {
            return new InMemoryHttpExchangeRepository();
        }
    }
}
