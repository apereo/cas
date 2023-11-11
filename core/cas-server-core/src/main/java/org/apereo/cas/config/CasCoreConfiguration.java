package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.CentralAuthenticationServiceContext;
import org.apereo.cas.DefaultCentralAuthenticationService;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationPolicy;
import org.apereo.cas.authentication.AuthenticationPolicyExecutionResult;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategyConfigurer;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.principal.DefaultServiceMatchingStrategy;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.ServiceMatchingStrategy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.lock.LockRepository;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.Ordered;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.List;

/**
 * This is {@link CasCoreConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = false)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Core)
@AutoConfiguration(after = CasCoreServicesConfiguration.class)
public class CasCoreConfiguration {

    @Configuration(value = "CasCorePolicyConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCorePolicyConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "serviceMatchingStrategy")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ServiceMatchingStrategy serviceMatchingStrategy(
            @Qualifier(ServicesManager.BEAN_NAME) final ServicesManager servicesManager) {
            return new DefaultServiceMatchingStrategy(servicesManager);
        }

        @Bean
        @ConditionalOnMissingBean(name = "globalAuthenticationPolicy")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationPolicy globalAuthenticationPolicy(final CasConfigurationProperties casProperties) {
            if (casProperties.getAuthn().getPolicy().isRequiredHandlerAuthenticationPolicyEnabled()) {
                LOGGER.trace("Applying configuration for Required Handler Authentication Policy");
                return (authentication, handlers, applicationContext, context) -> {
                    val registeredService = (RegisteredService) context.get(RegisteredService.class.getName());
                    val requiredHandlers = registeredService.getAuthenticationPolicy().getRequiredAuthenticationHandlers();
                    LOGGER.debug("Required authentication handlers for this service [{}] are [{}]",
                        registeredService.getName(), requiredHandlers);
                    val success = requiredHandlers
                        .stream()
                        .allMatch(required -> authentication.getSuccesses().containsKey(required));
                    return AuthenticationPolicyExecutionResult.success(success);
                };
            }
            return AuthenticationPolicy.alwaysSatisfied();
        }
    }

    @Configuration(value = "CasCoreContextConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreContextConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = CentralAuthenticationService.BEAN_NAME)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CentralAuthenticationService centralAuthenticationService(
            @Qualifier("centralAuthenticationServiceContext")
            final CentralAuthenticationServiceContext centralAuthenticationServiceContext) {
            return new DefaultCentralAuthenticationService(centralAuthenticationServiceContext);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CentralAuthenticationServiceContext centralAuthenticationServiceContext(
            @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
            final AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan,
            @Qualifier("protocolTicketCipherExecutor")
            final CipherExecutor cipherExecutor,
            @Qualifier(PrincipalFactory.BEAN_NAME)
            final PrincipalFactory principalFactory,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(TicketFactory.BEAN_NAME)
            final TicketFactory ticketFactory,
            @Qualifier(AuditableExecution.AUDITABLE_EXECUTION_REGISTERED_SERVICE_ACCESS)
            final AuditableExecution registeredServiceAccessStrategyEnforcer,
            @Qualifier("serviceMatchingStrategy")
            final ServiceMatchingStrategy serviceMatchingStrategy,
            @Qualifier("globalAuthenticationPolicy")
            final AuthenticationPolicy authenticationPolicy,
            @Qualifier(LockRepository.BEAN_NAME)
            final LockRepository casTicketRegistryLockRepository,
            final ConfigurableApplicationContext applicationContext) {
            return CentralAuthenticationServiceContext.builder()
                .authenticationServiceSelectionPlan(authenticationServiceSelectionPlan)
                .lockRepository(casTicketRegistryLockRepository)
                .cipherExecutor(cipherExecutor)
                .principalFactory(principalFactory)
                .ticketRegistry(ticketRegistry)
                .ticketFactory(ticketFactory)
                .registeredServiceAccessStrategyEnforcer(registeredServiceAccessStrategyEnforcer)
                .serviceMatchingStrategy(serviceMatchingStrategy)
                .applicationContext(applicationContext)
                .servicesManager(servicesManager)
                .authenticationPolicy(authenticationPolicy)
                .build();
        }
    }

    @Configuration(value = "CasCoreAuthenticationServiceSelectionConfiguration", proxyBeanMethods = false)
    @AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
    public static class CasCoreAuthenticationServiceSelectionConfiguration {
        @ConditionalOnMissingBean(name = AuthenticationServiceSelectionPlan.BEAN_NAME)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan(
            final List<AuthenticationServiceSelectionStrategyConfigurer> configurers) {
            val plan = new DefaultAuthenticationServiceSelectionPlan();
            configurers.forEach(c -> {
                LOGGER.trace("Configuring authentication request service selection strategy plan [{}]", c.getName());
                c.configureAuthenticationServiceSelectionStrategy(plan);
            });
            return plan;
        }
    }
}
