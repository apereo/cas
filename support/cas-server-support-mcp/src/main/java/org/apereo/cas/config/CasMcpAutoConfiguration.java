package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.api.AuthenticationRiskEvaluator;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlanConfigurer;
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
import org.apereo.cas.heimdall.authorizer.repository.AuthorizableResourceRepository;
import org.apereo.cas.interrupt.InterruptInquirer;
import org.apereo.cas.interrupt.InterruptInquiryExecutionPlan;
import org.apereo.cas.logout.slo.SingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.mcp.CasMcpEvaluationContextFactory;
import org.apereo.cas.mcp.audit.CasMcpAuditInterceptor;
import org.apereo.cas.mcp.audit.CasMcpAuditor;
import org.apereo.cas.mcp.audit.CasMcpOperationAuditActionResolver;
import org.apereo.cas.mcp.audit.CasMcpOperationAuditResourceResolver;
import org.apereo.cas.mcp.resources.CasSettingResources;
import org.apereo.cas.mcp.tools.CasAuditTools;
import org.apereo.cas.mcp.tools.CasAuthenticationEventTools;
import org.apereo.cas.mcp.tools.CasAuthenticationRiskTools;
import org.apereo.cas.mcp.tools.CasConsentTools;
import org.apereo.cas.mcp.tools.CasDelegatedAuthenticationTools;
import org.apereo.cas.mcp.tools.CasHeimdallTools;
import org.apereo.cas.mcp.tools.CasInterruptTools;
import org.apereo.cas.mcp.tools.CasMultifactorAuthenticationTools;
import org.apereo.cas.mcp.tools.CasOidcTools;
import org.apereo.cas.mcp.tools.CasOneTimeTokenTools;
import org.apereo.cas.mcp.tools.CasPasswordlessTools;
import org.apereo.cas.mcp.tools.CasPersonDirectoryTools;
import org.apereo.cas.mcp.tools.CasSamlIdPMetadataTools;
import org.apereo.cas.mcp.tools.CasServerStatisticsTools;
import org.apereo.cas.mcp.tools.CasServiceAccessTools;
import org.apereo.cas.mcp.tools.CasServiceManagementTools;
import org.apereo.cas.mcp.tools.CasSettingTools;
import org.apereo.cas.mcp.tools.CasSingleLogoutTools;
import org.apereo.cas.mcp.tools.CasSurrogateTools;
import org.apereo.cas.mcp.tools.CasTenantTools;
import org.apereo.cas.mcp.tools.CasTicketRegistryTools;
import org.apereo.cas.mcp.tools.CasTrustedDeviceTools;
import org.apereo.cas.mcp.tools.CasWebAuthnTools;
import org.apereo.cas.metadata.CasConfigurationMetadataRepository;
import org.apereo.cas.multitenancy.TenantsManager;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.oidc.jwks.generator.OidcJsonWebKeystoreGeneratorService;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviders;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.util.text.MessageSanitizer;
import org.apereo.cas.web.CasWebSecurityConfigurer;
import org.apereo.cas.webauthn.storage.WebAuthnCredentialRepository;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.modelcontextprotocol.server.McpAsyncServer;
import io.modelcontextprotocol.server.McpSyncServer;
import lombok.val;
import org.springframework.ai.mcp.annotation.McpResource;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.server.common.autoconfigure.McpServerAutoConfiguration;
import org.springframework.ai.mcp.server.common.autoconfigure.annotations.McpServerSpecificationFactoryAutoConfiguration;
import org.springframework.ai.mcp.server.common.autoconfigure.properties.McpServerStreamableHttpProperties;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.ComposablePointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.LazyInitializationExcludeFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Role;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
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
    @ConditionalOnMissingBean(name = "casMcpEvaluationContextFactory")
    public CasMcpEvaluationContextFactory casMcpEvaluationContextFactory(
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
        final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
        @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
        final PrincipalResolver principalResolver) {
        return new CasMcpEvaluationContextFactory(servicesManager, webApplicationServiceFactory, principalResolver);
    }

    @Bean
    @ConditionalOnMissingBean(name = "casServiceAccessTools")
    public CasServiceAccessTools casServiceAccessTools(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("casMcpEvaluationContextFactory")
        final CasMcpEvaluationContextFactory casMcpEvaluationContextFactory,
        @Qualifier(AuditableExecution.AUDITABLE_EXECUTION_REGISTERED_SERVICE_ACCESS)
        final AuditableExecution registeredServiceAccessStrategyEnforcer) {
        return new CasServiceAccessTools(casMcpEvaluationContextFactory, registeredServiceAccessStrategyEnforcer, applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean(name = "casTicketRegistryTools")
    public CasTicketRegistryTools casTicketRegistryTools(
        @Qualifier(TicketRegistry.BEAN_NAME)
        final TicketRegistry ticketRegistry) {
        return new CasTicketRegistryTools(ticketRegistry);
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
        @Qualifier("casMcpEvaluationContextFactory")
        final CasMcpEvaluationContextFactory casMcpEvaluationContextFactory,
        @Qualifier(MultifactorAuthenticationTriggerSelectionStrategy.BEAN_NAME)
        final ObjectProvider<MultifactorAuthenticationTriggerSelectionStrategy> triggerSelectionStrategy) {
        return new CasMultifactorAuthenticationTools(casMcpEvaluationContextFactory, triggerSelectionStrategy, applicationContext);
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

    @ConditionalOnClass(InterruptInquiryExecutionPlan.class)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.AI, module = "mcp-interrupt")
    @Configuration(value = "CasMcpInterruptConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasMcpInterruptConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "casInterruptTools")
        public CasInterruptTools casInterruptTools(
            @Qualifier("casMcpEvaluationContextFactory")
            final CasMcpEvaluationContextFactory casMcpEvaluationContextFactory,
            @Qualifier(InterruptInquirer.BEAN_NAME)
            final ObjectProvider<InterruptInquiryExecutionPlan> interruptInquiryExecutionPlan) {
            return new CasInterruptTools(casMcpEvaluationContextFactory, interruptInquiryExecutionPlan);
        }
    }

    @ConditionalOnClass(AuthenticationRiskEvaluator.class)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.AI, module = "mcp-electrofence")
    @Configuration(value = "CasMcpAuthenticationRiskConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasMcpAuthenticationRiskConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "casAuthenticationRiskTools")
        public CasAuthenticationRiskTools casAuthenticationRiskTools(
            final CasConfigurationProperties casProperties,
            @Qualifier("casMcpEvaluationContextFactory")
            final CasMcpEvaluationContextFactory casMcpEvaluationContextFactory,
            @Qualifier("authenticationRiskEvaluator")
            final ObjectProvider<AuthenticationRiskEvaluator> authenticationRiskEvaluator) {
            return new CasAuthenticationRiskTools(casMcpEvaluationContextFactory, authenticationRiskEvaluator, casProperties);
        }
    }

    @ConditionalOnClass(OneTimeTokenCredentialRepository.class)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.AI, module = "mcp-otp")
    @Configuration(value = "CasMcpOneTimeTokenConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasMcpOneTimeTokenConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "casOneTimeTokenTools")
        public CasOneTimeTokenTools casOneTimeTokenTools(
            @Qualifier("googleAuthenticatorAccountRegistry")
            final ObjectProvider<OneTimeTokenCredentialRepository> googleAuthenticatorAccountRegistry) {
            return new CasOneTimeTokenTools(googleAuthenticatorAccountRegistry);
        }
    }

    @ConditionalOnClass(WebAuthnCredentialRepository.class)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.AI, module = "mcp-webauthn")
    @Configuration(value = "CasMcpWebAuthnConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasMcpWebAuthnConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "casWebAuthnTools")
        public CasWebAuthnTools casWebAuthnTools(
            @Qualifier(WebAuthnCredentialRepository.BEAN_NAME)
            final ObjectProvider<WebAuthnCredentialRepository> webAuthnCredentialRepository) {
            return new CasWebAuthnTools(webAuthnCredentialRepository);
        }
    }

    @ConditionalOnClass(PasswordlessUserAccountStore.class)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.AI, module = "mcp-passwordless")
    @Configuration(value = "CasMcpPasswordlessConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasMcpPasswordlessConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "casPasswordlessTools")
        public CasPasswordlessTools casPasswordlessTools(
            @Qualifier(PasswordlessUserAccountStore.BEAN_NAME)
            final ObjectProvider<PasswordlessUserAccountStore> passwordlessUserAccountStore) {
            return new CasPasswordlessTools(passwordlessUserAccountStore);
        }
    }

    @ConditionalOnClass(DelegatedIdentityProviders.class)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.AI, module = "mcp-delegated")
    @Configuration(value = "CasMcpDelegatedAuthenticationConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasMcpDelegatedAuthenticationConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "casDelegatedAuthenticationTools")
        public CasDelegatedAuthenticationTools casDelegatedAuthenticationTools(
            @Qualifier("casMcpEvaluationContextFactory")
            final CasMcpEvaluationContextFactory casMcpEvaluationContextFactory,
            @Qualifier(DelegatedIdentityProviders.BEAN_NAME)
            final ObjectProvider<DelegatedIdentityProviders> delegatedIdentityProviders) {
            return new CasDelegatedAuthenticationTools(casMcpEvaluationContextFactory, delegatedIdentityProviders);
        }
    }

    @ConditionalOnClass(AuthorizableResourceRepository.class)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.AI, module = "mcp-heimdall")
    @Configuration(value = "CasMcpHeimdallConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasMcpHeimdallConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "casHeimdallTools")
        public CasHeimdallTools casHeimdallTools(
            @Qualifier(AuthorizableResourceRepository.BEAN_NAME)
            final ObjectProvider<AuthorizableResourceRepository> authorizableResourceRepository) {
            return new CasHeimdallTools(authorizableResourceRepository);
        }
    }

    @ConditionalOnClass(CasConfigurationMetadataRepository.class)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.AI, module = "mcp-settings")
    @Configuration(value = "CasMcpSettingsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasMcpSettingsConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "casSettingResources")
        public CasSettingResources casSettingResources(
            final Environment environment,
            @Qualifier(CasConfigurationMetadataRepository.BEAN_NAME)
            final ObjectProvider<CasConfigurationMetadataRepository> configurationMetadataRepository) {
            return new CasSettingResources(configurationMetadataRepository, environment);
        }

        @Bean
        @ConditionalOnMissingBean(name = "casSettingTools")
        public CasSettingTools casSettingTools(
            @Qualifier(CasConfigurationMetadataRepository.BEAN_NAME)
            final ObjectProvider<CasConfigurationMetadataRepository> configurationMetadataRepository) {
            return new CasSettingTools(configurationMetadataRepository);
        }
    }

    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.AI, module = "mcp-audit")
    @Configuration(value = "CasMcpAuditConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasMcpAuditConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "casMcpAuditor")
        public CasMcpAuditor casMcpAuditor() {
            return new CasMcpAuditor();
        }

        @Bean
        @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
        @ConditionalOnMissingBean(name = "casMcpAuditAdvisor")
        public static Advisor casMcpAuditAdvisor(
            @Qualifier("casMcpAuditor")
            final ObjectProvider<CasMcpAuditor> casMcpAuditor) {
            val pointcut = new ComposablePointcut(new AnnotationMatchingPointcut(null, McpTool.class, true))
                .union(new AnnotationMatchingPointcut(null, McpResource.class, true));
            val advisor = new DefaultPointcutAdvisor(pointcut, new CasMcpAuditInterceptor(casMcpAuditor));
            advisor.setOrder(Ordered.HIGHEST_PRECEDENCE);
            return advisor;
        }

        @Bean
        @ConditionalOnMissingBean(name = "casMcpAuditTrailRecordResolutionPlanConfigurer")
        public AuditTrailRecordResolutionPlanConfigurer casMcpAuditTrailRecordResolutionPlanConfigurer(
            @Qualifier(MessageSanitizer.BEAN_NAME)
            final ObjectProvider<MessageSanitizer> messageSanitizer) {
            return plan -> {
                plan.registerAuditActionResolver(CasMcpAuditor.AUDIT_ACTION_RESOLVER, new CasMcpOperationAuditActionResolver());
                plan.registerAuditResourceResolver(CasMcpAuditor.AUDIT_RESOURCE_RESOLVER,
                    new CasMcpOperationAuditResourceResolver(messageSanitizer.getIfAvailable(MessageSanitizer::disabled)));
            };
        }
    }
}
