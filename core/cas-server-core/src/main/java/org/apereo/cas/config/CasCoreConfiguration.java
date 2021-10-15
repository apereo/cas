package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.DefaultCentralAuthenticationService;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategyConfigurer;
import org.apereo.cas.authentication.ContextualAuthenticationPolicyFactory;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.policy.AcceptAnyAuthenticationPolicyFactory;
import org.apereo.cas.authentication.policy.RequiredHandlerAuthenticationPolicyFactory;
import org.apereo.cas.authentication.principal.DefaultServiceMatchingStrategy;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.ServiceMatchingStrategy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServiceContext;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.List;

/**
 * This is {@link CasCoreConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "casCoreConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement
@Slf4j
@AutoConfigureAfter(CasCoreServicesConfiguration.class)
public class CasCoreConfiguration {

    @Configuration(value = "CasCorePolicyConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCorePolicyConfiguration {
        @Bean
        @Autowired
        @ConditionalOnMissingBean(name = "authenticationPolicyFactory")
        public ContextualAuthenticationPolicyFactory<ServiceContext> authenticationPolicyFactory(
            final CasConfigurationProperties casProperties) {
            if (casProperties.getAuthn().getPolicy().isRequiredHandlerAuthenticationPolicyEnabled()) {
                LOGGER.trace("Applying configuration for Required Handler Authentication Policy");
                return new RequiredHandlerAuthenticationPolicyFactory();
            }
            LOGGER.trace("Applying configuration for Accept Any Authentication Policy");
            return new AcceptAnyAuthenticationPolicyFactory();
        }

        @Bean
        @ConditionalOnMissingBean(name = "serviceMatchingStrategy")
        @Autowired
        public ServiceMatchingStrategy serviceMatchingStrategy(
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            return new DefaultServiceMatchingStrategy(servicesManager);
        }
    }

    @Configuration(value = "CasCoreContextConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreContextConfiguration {

        @Bean
        @Autowired
        @ConditionalOnMissingBean(name = CentralAuthenticationService.BEAN_NAME)
        public CentralAuthenticationService centralAuthenticationService(
            @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
            final AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan,
            @Qualifier("protocolTicketCipherExecutor")
            final CipherExecutor cipherExecutor,
            @Qualifier("principalFactory")
            final PrincipalFactory principalFactory,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier("defaultTicketFactory")
            final TicketFactory ticketFactory,
            @Qualifier("registeredServiceAccessStrategyEnforcer")
            final AuditableExecution registeredServiceAccessStrategyEnforcer,
            @Qualifier("authenticationPolicyFactory")
            final ContextualAuthenticationPolicyFactory<ServiceContext> authenticationPolicyFactory,
            @Qualifier("serviceMatchingStrategy")
            final ServiceMatchingStrategy serviceMatchingStrategy,
            final ConfigurableApplicationContext applicationContext) {
            return new DefaultCentralAuthenticationService(applicationContext,
                ticketRegistry, servicesManager, ticketFactory,
                authenticationServiceSelectionPlan, authenticationPolicyFactory, principalFactory,
                cipherExecutor, registeredServiceAccessStrategyEnforcer, serviceMatchingStrategy);
        }
    }

    @Configuration(value = "CasCoreAuthenticationServiceSelectionConfiguration", proxyBeanMethods = false)
    @AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
    public static class CasCoreAuthenticationServiceSelectionConfiguration {
        @ConditionalOnMissingBean(name = AuthenticationServiceSelectionPlan.BEAN_NAME)
        @Autowired
        @Bean
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
