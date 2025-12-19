package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.validation.DelegatedAuthenticationServiceTicketValidationAuthorizer;
import org.apereo.cas.validation.RegisteredServiceDelegatedAuthenticationPolicyAuditableEnforcer;
import org.apereo.cas.validation.ServiceTicketValidationAuthorizer;
import org.apereo.cas.validation.ServiceTicketValidationAuthorizerConfigurer;
import org.apereo.cas.web.flow.DelegatedAuthenticationSingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategyConfigurer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link DelegatedAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.DelegatedAuthentication)
@Configuration(value = "DelegatedAuthenticationConfiguration", proxyBeanMethods = false)
class DelegatedAuthenticationConfiguration {
    @Configuration(value = "DelegatedAuthenticationBaseConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class DelegatedAuthenticationBaseConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = AuditableExecution.AUDITABLE_EXECUTION_DELEGATED_AUTHENTICATION_ACCESS)
        public AuditableExecution registeredServiceDelegatedAuthenticationPolicyAuditableEnforcer() {
            return new RegisteredServiceDelegatedAuthenticationPolicyAuditableEnforcer();
        }
    }

    @Configuration(value = "DelegatedAuthenticationAuthorizerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class DelegatedAuthenticationAuthorizerConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ServiceTicketValidationAuthorizer pac4jServiceTicketValidationAuthorizer(
            @Qualifier(AuditableExecution.AUDITABLE_EXECUTION_DELEGATED_AUTHENTICATION_ACCESS)
            final AuditableExecution registeredServiceDelegatedAuthenticationPolicyAuditableEnforcer,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            return new DelegatedAuthenticationServiceTicketValidationAuthorizer(servicesManager,
                registeredServiceDelegatedAuthenticationPolicyAuditableEnforcer);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ServiceTicketValidationAuthorizerConfigurer pac4jServiceTicketValidationAuthorizerConfigurer(
            @Qualifier("pac4jServiceTicketValidationAuthorizer")
            final ServiceTicketValidationAuthorizer pac4jServiceTicketValidationAuthorizer) {
            return plan -> plan.registerAuthorizer(pac4jServiceTicketValidationAuthorizer);
        }
    }

    @Configuration(value = "DelegatedAuthenticationSingleSignOnConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class DelegatedAuthenticationSingleSignOnConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "pac4jDelegatedAuthenticationSingleSignOnParticipationStrategy")
        public SingleSignOnParticipationStrategy pac4jDelegatedAuthenticationSingleSignOnParticipationStrategy(
            @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
            final AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan,
            @Qualifier(TicketRegistrySupport.BEAN_NAME)
            final TicketRegistrySupport ticketRegistrySupport,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            return new DelegatedAuthenticationSingleSignOnParticipationStrategy(servicesManager,
                authenticationServiceSelectionPlan, ticketRegistrySupport);
        }

        @Bean
        @ConditionalOnMissingBean(name = "pac4jDelegatedAuthenticationSingleSignOnParticipationStrategyConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SingleSignOnParticipationStrategyConfigurer pac4jDelegatedAuthenticationSingleSignOnParticipationStrategyConfigurer(
            @Qualifier("pac4jDelegatedAuthenticationSingleSignOnParticipationStrategy")
            final SingleSignOnParticipationStrategy pac4jDelegatedAuthenticationSingleSignOnParticipationStrategy) {
            return chain -> chain.addStrategy(pac4jDelegatedAuthenticationSingleSignOnParticipationStrategy);
        }
    }
}
