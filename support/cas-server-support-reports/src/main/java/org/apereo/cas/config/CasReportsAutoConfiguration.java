package org.apereo.cas.config;

import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.logout.slo.SingleLogoutRequestExecutor;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.ServicesManagerConfigurationContext;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.services.util.RegisteredServiceYamlSerializer;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.proxy.ProxyHandler;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistryCleaner;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.feature.CasRuntimeModuleLoader;
import org.apereo.cas.util.spring.DirectObjectProvider;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.validation.CasProtocolValidationSpecification;
import org.apereo.cas.web.ServiceValidateConfigurationContext;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.report.AttributeDefinitionsEndpoint;
import org.apereo.cas.web.report.AuditLogEndpoint;
import org.apereo.cas.web.report.CasConfigurationEndpoint;
import org.apereo.cas.web.report.CasFeaturesEndpoint;
import org.apereo.cas.web.report.CasInfoEndpointContributor;
import org.apereo.cas.web.report.CasProtocolValidationEndpoint;
import org.apereo.cas.web.report.CasReleaseAttributesReportEndpoint;
import org.apereo.cas.web.report.CasResolveAttributesReportEndpoint;
import org.apereo.cas.web.report.CasRuntimeModulesEndpoint;
import org.apereo.cas.web.report.MultifactorAuthenticationDevicesEndpoint;
import org.apereo.cas.web.report.MultitenancyEndpoint;
import org.apereo.cas.web.report.RegisteredAuthenticationHandlersEndpoint;
import org.apereo.cas.web.report.RegisteredAuthenticationPoliciesEndpoint;
import org.apereo.cas.web.report.RegisteredServiceAccessEndpoint;
import org.apereo.cas.web.report.RegisteredServicesEndpoint;
import org.apereo.cas.web.report.SingleSignOnSessionStatusEndpoint;
import org.apereo.cas.web.report.SingleSignOnSessionsEndpoint;
import org.apereo.cas.web.report.SpringWebflowEndpoint;
import org.apereo.cas.web.report.StatisticsEndpoint;
import org.apereo.cas.web.report.TicketExpirationPoliciesEndpoint;
import org.apereo.cas.web.report.TicketRegistryEndpoint;
import org.apereo.cas.web.support.ArgumentExtractor;
import lombok.val;
import org.apereo.inspektr.common.spi.AuditActionDateProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.autoconfigure.info.ConditionalOnEnabledInfoContributor;
import org.springframework.boot.actuate.autoconfigure.info.InfoContributorFallback;
import org.springframework.boot.actuate.info.InfoContributor;
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
 * This this {@link CasReportsAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.3.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Reports)
@AutoConfiguration
public class CasReportsAutoConfiguration {

    @Configuration(value = "AttributesEndpointsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class AttributesEndpointsConfiguration {
        @Bean
        @ConditionalOnAvailableEndpoint
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AttributeDefinitionsEndpoint attributeDefinitionsEndpoint(
            @Qualifier(AttributeDefinitionStore.BEAN_NAME)
            final ObjectProvider<AttributeDefinitionStore> attributeDefinitionStore,
            final CasConfigurationProperties casProperties) {
            return new AttributeDefinitionsEndpoint(casProperties, attributeDefinitionStore);
        }
        
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
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
            final ObjectProvider<PrincipalResolver> defaultPrincipalResolver,
            @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
            final ObjectProvider<ServiceFactory<WebApplicationService>> webApplicationServiceFactory,
            @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
            final ObjectProvider<AuthenticationSystemSupport> authenticationSystemSupport,
            @Qualifier(PrincipalFactory.BEAN_NAME)
            final ObjectProvider<PrincipalFactory> principalFactory,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ObjectProvider<ServicesManager> servicesManager,
            final CasConfigurationProperties casProperties) {
            return new CasReleaseAttributesReportEndpoint(
                casProperties, applicationContext,
                servicesManager, authenticationSystemSupport,
                webApplicationServiceFactory, principalFactory, defaultPrincipalResolver);
        }

        @Bean
        @ConditionalOnAvailableEndpoint
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasProtocolValidationEndpoint casProtocolValidationEndpoint(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("casValidationConfigurationContext")
            final ServiceValidateConfigurationContext casValidationConfigurationContext,
            @Qualifier("proxy20Handler")
            final ProxyHandler proxy20Handler,
            @Qualifier("v3ServiceValidateControllerValidationSpecification")
            final CasProtocolValidationSpecification v3ServiceValidateControllerValidationSpecification) {
            return new CasProtocolValidationEndpoint(casProperties, casValidationConfigurationContext
                .withValidationSpecifications(CollectionUtils.wrapSet(v3ServiceValidateControllerValidationSpecification))
                .withProxyHandler(proxy20Handler));
        }
    }

    @Configuration(value = "SingleSignOnEndpointsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SingleSignOnEndpointsConfiguration {
        @Bean
        @ConditionalOnAvailableEndpoint
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SingleSignOnSessionsEndpoint singleSignOnSessionsEndpoint(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final ObjectProvider<TicketRegistry> ticketRegistry,
            @Qualifier(SingleLogoutRequestExecutor.BEAN_NAME)
            final ObjectProvider<SingleLogoutRequestExecutor> defaultSingleLogoutRequestExecutor,
            final CasConfigurationProperties casProperties) {
            return new SingleSignOnSessionsEndpoint(ticketRegistry, applicationContext,
                casProperties, defaultSingleLogoutRequestExecutor);
        }

        @Bean
        @ConditionalOnAvailableEndpoint
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SingleSignOnSessionStatusEndpoint singleSignOnSessionStatusEndpoint(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasCookieBuilder.BEAN_NAME_TICKET_GRANTING_COOKIE_BUILDER)
            final ObjectProvider<CasCookieBuilder> ticketGrantingTicketCookieGenerator,
            @Qualifier(TicketRegistrySupport.BEAN_NAME)
            final ObjectProvider<TicketRegistrySupport> ticketRegistrySupport) {
            return new SingleSignOnSessionStatusEndpoint(casProperties, applicationContext,
                ticketGrantingTicketCookieGenerator, ticketRegistrySupport);
        }
    }

    @Configuration(value = "AuthenticationEndpointsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class AuthenticationEndpointsConfiguration {
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
    static class SystemInfoEndpointsConfiguration {
        @Bean
        @ConditionalOnAvailableEndpoint
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasConfigurationEndpoint casConfigurationEndpoint(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("casConfigurationCipherExecutor")
            final CipherExecutor<String, String> casConfigurationCipherExecutor) {
            return new CasConfigurationEndpoint(casProperties, applicationContext, casConfigurationCipherExecutor);
        }


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
        @ConditionalOnEnabledInfoContributor(value = "cas", fallback = InfoContributorFallback.DISABLE)
        public InfoContributor casInfoEndpointContributor(
            final CasConfigurationProperties casProperties,
            @Qualifier("casRuntimeModuleLoader")
            final CasRuntimeModuleLoader casRuntimeModuleLoader) {
            return new CasInfoEndpointContributor(casProperties, casRuntimeModuleLoader);
        }
    }

    @Configuration(value = "RegisteredServicesEndpointsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class RegisteredServicesEndpointsConfiguration {
        @Bean
        @ConditionalOnAvailableEndpoint
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public RegisteredServicesEndpoint registeredServicesReportEndpoint(
            final ObjectProvider<ConfigurableApplicationContext> applicationContext,
            @Qualifier(ServicesManagerConfigurationContext.BEAN_NAME)
            final ObjectProvider<ServicesManagerConfigurationContext> configurationContext,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ObjectProvider<ServicesManager> servicesManager,
            final CasConfigurationProperties casProperties) {

            val serializers = CollectionUtils.wrapList(
                new RegisteredServiceYamlSerializer(applicationContext.getObject()),
                new RegisteredServiceJsonSerializer(applicationContext.getObject()));
            return new RegisteredServicesEndpoint(casProperties,
                servicesManager,
                configurationContext,
                new DirectObjectProvider<>(serializers),
                applicationContext);
        }

        @Bean
        @ConditionalOnAvailableEndpoint
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public RegisteredServiceAccessEndpoint registeredServiceAccessEndpoint(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(ArgumentExtractor.BEAN_NAME)
            final ObjectProvider<ArgumentExtractor> argumentExtractor,
            @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
            final ObjectProvider<AuthenticationServiceSelectionPlan> authenticationServiceSelectionPlan,
            final CasConfigurationProperties casProperties,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ObjectProvider<ServicesManager> servicesManager,
            @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
            final ObjectProvider<AuthenticationSystemSupport> authenticationSystemSupport,
            @Qualifier(AuditableExecution.AUDITABLE_EXECUTION_REGISTERED_SERVICE_ACCESS)
            final ObjectProvider<AuditableExecution> registeredServiceAccessStrategyEnforcer,
            @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
            final ObjectProvider<PrincipalResolver> defaultPrincipalResolver,
            @Qualifier(PrincipalFactory.BEAN_NAME)
            final ObjectProvider<PrincipalFactory> principalFactory) {
            return new RegisteredServiceAccessEndpoint(casProperties, applicationContext, servicesManager,
                authenticationServiceSelectionPlan, argumentExtractor, registeredServiceAccessStrategyEnforcer,
                authenticationSystemSupport, defaultPrincipalResolver, principalFactory);
        }
    }

    @Configuration(value = "AuditActivityEndpointsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class AuditActivityEndpointsConfiguration {
        @Bean
        @ConditionalOnAvailableEndpoint
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditLogEndpoint auditLogEndpoint(
            @Qualifier("defaultAuditActionDateProvider")
            final ObjectProvider<AuditActionDateProvider> defaultAuditActionDateProvider,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(AuditTrailExecutionPlan.BEAN_NAME)
            final ObjectProvider<AuditTrailExecutionPlan> auditTrailExecutionPlan,
            final CasConfigurationProperties casProperties) {
            return new AuditLogEndpoint(auditTrailExecutionPlan,
                applicationContext, defaultAuditActionDateProvider, casProperties);
        }
    }

    @Configuration(value = "TicketingEndpointsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class TicketingEndpointsConfiguration {
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
        @ConditionalOnAvailableEndpoint
        public TicketRegistryEndpoint ticketRegistryEndpoint(
            @Qualifier(TicketCatalog.BEAN_NAME)
            final ObjectProvider<TicketCatalog> ticketCatalog,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(TicketRegistrySupport.BEAN_NAME)
            final ObjectProvider<TicketRegistrySupport> ticketRegistrySupport,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final ObjectProvider<TicketRegistry> ticketRegistry,
            @Qualifier(TicketRegistryCleaner.BEAN_NAME)
            final ObjectProvider<TicketRegistryCleaner> ticketRegistryCleaner) {
            return new TicketRegistryEndpoint(casProperties, applicationContext,
                ticketRegistry, ticketRegistryCleaner, ticketRegistrySupport,
                ticketCatalog);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnAvailableEndpoint(endpoint = HttpExchangesEndpoint.class)
        public HttpExchangeRepository exchangeRepository() {
            return new InMemoryHttpExchangeRepository();
        }
    }

    @Configuration(value = "MultifactorDevicesEndpointsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class MultifactorDevicesEndpointsConfiguration {
        @Bean
        @ConditionalOnAvailableEndpoint
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public MultifactorAuthenticationDevicesEndpoint multifactorAuthenticationDevicesEndpoint(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return new MultifactorAuthenticationDevicesEndpoint(casProperties, applicationContext);
        }
    }

    @Configuration(value = "MultitenancyEndpointsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Multitenancy)
    static class MultitenancyEndpointsConfiguration {
        @Bean
        @ConditionalOnAvailableEndpoint
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public MultitenancyEndpoint multitenancyEndpoint(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(TenantExtractor.BEAN_NAME)
            final ObjectProvider<TenantExtractor> tenantExtractor,
            final CasConfigurationProperties casProperties) {
            return new MultitenancyEndpoint(casProperties, applicationContext, tenantExtractor);
        }
    }
}
