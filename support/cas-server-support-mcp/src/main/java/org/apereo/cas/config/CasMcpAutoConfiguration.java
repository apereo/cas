package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.MultifactorAuthenticationTriggerSelectionStrategy;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.consent.ConsentDecisionBuilder;
import org.apereo.cas.consent.ConsentRepository;
import org.apereo.cas.logout.slo.SingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.mcp.tools.CasAuditTools;
import org.apereo.cas.mcp.tools.CasAuthenticationEventTools;
import org.apereo.cas.mcp.tools.CasConsentTools;
import org.apereo.cas.mcp.tools.CasMultifactorAuthenticationTools;
import org.apereo.cas.mcp.tools.CasOidcTools;
import org.apereo.cas.mcp.tools.CasPersonDirectoryTools;
import org.apereo.cas.mcp.tools.CasSamlIdPMetadataTools;
import org.apereo.cas.mcp.tools.CasServerStatisticsTools;
import org.apereo.cas.mcp.tools.CasServiceAccessTools;
import org.apereo.cas.mcp.tools.CasServiceManagementTools;
import org.apereo.cas.mcp.tools.CasSingleLogoutTools;
import org.apereo.cas.mcp.tools.CasSurrogateTools;
import org.apereo.cas.mcp.tools.CasTenantTools;
import org.apereo.cas.mcp.tools.CasTicketRegistryTools;
import org.apereo.cas.mcp.tools.CasTrustedDeviceTools;
import org.apereo.cas.multitenancy.TenantsManager;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.oidc.jwks.generator.OidcJsonWebKeystoreGeneratorService;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.CasWebSecurityConfigurer;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.modelcontextprotocol.server.McpAsyncServer;
import io.modelcontextprotocol.server.McpSyncServer;
import lombok.val;
import org.springframework.ai.mcp.server.common.autoconfigure.McpServerAutoConfiguration;
import org.springframework.ai.mcp.server.common.autoconfigure.annotations.McpServerSpecificationFactoryAutoConfiguration;
import org.springframework.ai.mcp.server.common.autoconfigure.properties.McpServerStreamableHttpProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.LazyInitializationExcludeFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

/**
 * This is {@link CasMcpAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@EnableConfigurationProperties({CasConfigurationProperties.class, McpServerStreamableHttpProperties.class})
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.AI, module = "mcp")
@AutoConfiguration(before = {
    McpServerAutoConfiguration.class,
    McpServerSpecificationFactoryAutoConfiguration.class
})
@Lazy(false)
public class CasMcpAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = "casMcpLazyInitializationExcludeFilter")
    public static LazyInitializationExcludeFilter casMcpLazyInitializationExcludeFilter() {
        return LazyInitializationExcludeFilter.forBeanTypes(McpSyncServer.class, McpAsyncServer.class);
    }

    @Bean
    @ConditionalOnMissingBean(name = "casServiceManagementTools")
    public CasServiceManagementTools casServiceManagementTools(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
        final ServiceFactory<WebApplicationService> webApplicationServiceFactory) {
        return new CasServiceManagementTools(servicesManager, webApplicationServiceFactory,
            new RegisteredServiceJsonSerializer(applicationContext));
    }

    @Bean
    @ConditionalOnMissingBean(name = "casServiceAccessTools")
    public CasServiceAccessTools casServiceAccessTools(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
        final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
        @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
        final PrincipalResolver principalResolver,
        @Qualifier(AuditableExecution.AUDITABLE_EXECUTION_REGISTERED_SERVICE_ACCESS)
        final AuditableExecution registeredServiceAccessStrategyEnforcer) {
        return new CasServiceAccessTools(servicesManager, webApplicationServiceFactory,
            principalResolver, registeredServiceAccessStrategyEnforcer, applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean(name = "casTicketRegistryTools")
    public CasTicketRegistryTools casTicketRegistryTools(
        @Qualifier(TicketRegistry.BEAN_NAME)
        final TicketRegistry ticketRegistry,
        @Qualifier(TicketSerializationManager.BEAN_NAME)
        final TicketSerializationManager ticketSerializationManager) {
        return new CasTicketRegistryTools(ticketRegistry, ticketSerializationManager);
    }

    @Bean
    @ConditionalOnMissingBean(name = "casPersonDirectoryTools")
    public CasPersonDirectoryTools casPersonDirectoryTools(
        @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
        final PrincipalResolver principalResolver) {
        return new CasPersonDirectoryTools(principalResolver);
    }

    @Bean
    @ConditionalOnMissingBean(name = "casMultifactorAuthenticationTools")
    public CasMultifactorAuthenticationTools casMultifactorAuthenticationTools(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
        final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
        @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
        final PrincipalResolver principalResolver,
        @Qualifier(MultifactorAuthenticationTriggerSelectionStrategy.BEAN_NAME)
        final ObjectProvider<MultifactorAuthenticationTriggerSelectionStrategy> triggerSelectionStrategy) {
        return new CasMultifactorAuthenticationTools(servicesManager, webApplicationServiceFactory,
            principalResolver, triggerSelectionStrategy, applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean(name = "casSingleLogoutTools")
    public CasSingleLogoutTools casSingleLogoutTools(
        final CasConfigurationProperties casProperties,
        @Qualifier(TicketRegistry.BEAN_NAME)
        final TicketRegistry ticketRegistry,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        @Qualifier("singleLogoutServiceLogoutUrlBuilder")
        final SingleLogoutServiceLogoutUrlBuilder singleLogoutServiceLogoutUrlBuilder) {
        return new CasSingleLogoutTools(ticketRegistry, servicesManager, singleLogoutServiceLogoutUrlBuilder, casProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "casServerStatisticsTools")
    public CasServerStatisticsTools casServerStatisticsTools(
        @Qualifier(TicketRegistry.BEAN_NAME)
        final TicketRegistry ticketRegistry,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager) {
        return new CasServerStatisticsTools(ticketRegistry, servicesManager);
    }

    @Bean
    @ConditionalOnMissingBean(name = "casTenantTools")
    public CasTenantTools casTenantTools(
        @Qualifier(TenantsManager.BEAN_NAME)
        final ObjectProvider<TenantsManager> tenantsManager) {
        return new CasTenantTools(tenantsManager);
    }

    @Bean
    @ConditionalOnMissingBean(name = "casAuditTools")
    public CasAuditTools casAuditTools(
        @Qualifier(AuditTrailExecutionPlan.BEAN_NAME)
        final AuditTrailExecutionPlan auditTrailExecutionPlan) {
        return new CasAuditTools(auditTrailExecutionPlan);
    }

    @Bean
    @ConditionalOnMissingBean(name = "casAuthenticationEventTools")
    public CasAuthenticationEventTools casAuthenticationEventTools(
        @Qualifier(CasEventRepository.BEAN_NAME)
        final ObjectProvider<CasEventRepository> casEventRepository) {
        return new CasAuthenticationEventTools(casEventRepository);
    }

    @Bean
    @ConditionalOnMissingBean(name = "casMcpEndpointConfigurer")
    public CasWebSecurityConfigurer<HttpSecurity> casMcpEndpointConfigurer(
        final McpServerStreamableHttpProperties mcpServerStreamableHttpProperties) {
        val endpoint = mcpServerStreamableHttpProperties.getMcpEndpoint();
        return new CasWebSecurityConfigurer<>() {
            @Override
            @CanIgnoreReturnValue
            public CasWebSecurityConfigurer<HttpSecurity> configure(final HttpSecurity http) throws Exception {
                http.authorizeHttpRequests(customizer -> customizer
                        .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher(endpoint)).authenticated())
                    .httpBasic(Customizer.withDefaults());
                return this;
            }
        };
    }

    @ConditionalOnClass(ConsentRepository.class)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.AI, module = "mcp-consent")
    @Configuration(value = "CasMcpConsentConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasMcpConsentConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "casConsentTools")
        public CasConsentTools casConsentTools(
            @Qualifier(ConsentRepository.BEAN_NAME)
            final ObjectProvider<ConsentRepository> consentRepository,
            @Qualifier(ConsentDecisionBuilder.BEAN_NAME)
            final ObjectProvider<ConsentDecisionBuilder> consentDecisionBuilder) {
            return new CasConsentTools(consentRepository, consentDecisionBuilder);
        }
    }

    @ConditionalOnClass(SamlRegisteredServiceCachingMetadataResolver.class)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.AI, module = "mcp-saml-idp")
    @Configuration(value = "CasMcpSamlIdPConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasMcpSamlIdPConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "casSamlIdPMetadataTools")
        public CasSamlIdPMetadataTools casSamlIdPMetadataTools(
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(SamlRegisteredServiceCachingMetadataResolver.BEAN_NAME)
            final ObjectProvider<SamlRegisteredServiceCachingMetadataResolver> metadataResolver) {
            return new CasSamlIdPMetadataTools(servicesManager, metadataResolver);
        }
    }

    @ConditionalOnClass({OidcIssuerService.class, OidcRegisteredService.class})
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.AI, module = "mcp-oidc")
    @Configuration(value = "CasMcpOidcConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasMcpOidcConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "casOidcTools")
        public CasOidcTools casOidcTools(
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(OidcIssuerService.BEAN_NAME)
            final ObjectProvider<OidcIssuerService> oidcIssuerService,
            @Qualifier("oidcJsonWebKeystoreGeneratorService")
            final ObjectProvider<OidcJsonWebKeystoreGeneratorService> oidcJsonWebKeystoreGeneratorService) {
            return new CasOidcTools(servicesManager, oidcIssuerService, oidcJsonWebKeystoreGeneratorService);
        }
    }

    @ConditionalOnClass(MultifactorAuthenticationTrustStorage.class)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.AI, module = "mcp-trusted-mfa")
    @Configuration(value = "CasMcpTrustedDevicesConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasMcpTrustedDevicesConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "casTrustedDeviceTools")
        public CasTrustedDeviceTools casTrustedDeviceTools(
            @Qualifier(MultifactorAuthenticationTrustStorage.BEAN_NAME)
            final ObjectProvider<MultifactorAuthenticationTrustStorage> mfaTrustEngine) {
            return new CasTrustedDeviceTools(mfaTrustEngine);
        }
    }

    @ConditionalOnClass(SurrogateAuthenticationService.class)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.AI, module = "mcp-surrogate")
    @Configuration(value = "CasMcpSurrogateConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasMcpSurrogateConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "casSurrogateTools")
        public CasSurrogateTools casSurrogateTools(
            @Qualifier(SurrogateAuthenticationService.BEAN_NAME)
            final ObjectProvider<SurrogateAuthenticationService> surrogateAuthenticationService,
            @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory) {
            return new CasSurrogateTools(surrogateAuthenticationService, webApplicationServiceFactory);
        }
    }
}
