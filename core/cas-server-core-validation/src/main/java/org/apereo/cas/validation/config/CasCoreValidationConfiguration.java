package org.apereo.cas.validation.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.proxy.ProxyHandler;
import org.apereo.cas.ticket.proxy.support.Cas10ProxyHandler;
import org.apereo.cas.ticket.proxy.support.Cas20ProxyHandler;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.validation.AuthenticationPolicyAwareServiceTicketValidationAuthorizer;
import org.apereo.cas.validation.Cas10ProtocolValidationSpecification;
import org.apereo.cas.validation.Cas20ProtocolValidationSpecification;
import org.apereo.cas.validation.Cas20WithoutProxyingValidationSpecification;
import org.apereo.cas.validation.CasProtocolValidationSpecification;
import org.apereo.cas.validation.DefaultServiceTicketValidationAuthorizersExecutionPlan;
import org.apereo.cas.validation.ServiceTicketValidationAuthorizer;
import org.apereo.cas.validation.ServiceTicketValidationAuthorizerConfigurer;
import org.apereo.cas.validation.ServiceTicketValidationAuthorizersExecutionPlan;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.List;

/**
 * This is {@link CasCoreValidationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "casCoreValidationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasCoreValidationConfiguration {

    @Configuration(value = "CasCoreValidationProxyConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreValidationProxyConfiguration {
        @ConditionalOnMissingBean(name = "proxy10Handler")
        @Bean
        @ConditionalOnProperty(prefix = "cas.sso", name = "proxy-authn-enabled", havingValue = "true", matchIfMissing = true)
        public ProxyHandler proxy10Handler() {
            return new Cas10ProxyHandler();
        }

        @ConditionalOnMissingBean(name = "proxy20Handler")
        @Bean
        @Autowired
        @ConditionalOnProperty(prefix = "cas.sso", name = "proxy-authn-enabled", havingValue = "true", matchIfMissing = true)
        public ProxyHandler proxy20Handler(
            @Qualifier("proxy20TicketUniqueIdGenerator")
            final UniqueTicketIdGenerator proxy20TicketUniqueIdGenerator,
            @Qualifier("supportsTrustStoreSslSocketFactoryHttpClient")
            final HttpClient httpClient) {
            return new Cas20ProxyHandler(httpClient, proxy20TicketUniqueIdGenerator);
        }
    }


    @Configuration(value = "CasCoreValidationSpecificationConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreValidationSpecificationConfiguration {
        @Bean
        @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        @Autowired
        @ConditionalOnMissingBean(name = "cas10ProtocolValidationSpecification")
        public CasProtocolValidationSpecification cas10ProtocolValidationSpecification(
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            return new Cas10ProtocolValidationSpecification(servicesManager);
        }

        @Bean
        @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        @Autowired
        @ConditionalOnMissingBean(name = "cas20ProtocolValidationSpecification")
        public CasProtocolValidationSpecification cas20ProtocolValidationSpecification(
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            return new Cas20ProtocolValidationSpecification(servicesManager);
        }

        @Bean
        @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        @ConditionalOnMissingBean(name = "cas20WithoutProxyProtocolValidationSpecification")
        @Autowired
        public CasProtocolValidationSpecification cas20WithoutProxyProtocolValidationSpecification(
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            return new Cas20WithoutProxyingValidationSpecification(servicesManager);
        }

    }


    @Configuration(value = "CasCoreValidationExecutionPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreValidationExecutionPlanConfiguration {
        @Autowired
        @Bean
        @ConditionalOnMissingBean(name = "serviceValidationAuthorizers")
        public ServiceTicketValidationAuthorizersExecutionPlan serviceValidationAuthorizers(final List<ServiceTicketValidationAuthorizerConfigurer> configurers) {
            val plan = new DefaultServiceTicketValidationAuthorizersExecutionPlan();
            configurers.forEach(c -> {
                LOGGER.trace("Configuring service ticket validation authorizer execution plan [{}]", c.getName());
                c.configureAuthorizersExecutionPlan(plan);
            });
            return plan;
        }
    }


    @Configuration(value = "CasCoreValidationAuthorizerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreValidationAuthorizerConfiguration {
        @Bean
        @Autowired
        @ConditionalOnMissingBean(name = "casCoreServiceTicketValidationAuthorizerConfigurer")
        public ServiceTicketValidationAuthorizerConfigurer casCoreServiceTicketValidationAuthorizerConfigurer(
            @Qualifier("authenticationPolicyAwareServiceTicketValidationAuthorizer")
            final ServiceTicketValidationAuthorizer authenticationPolicyAwareServiceTicketValidationAuthorizer) {
            return plan -> plan.registerAuthorizer(authenticationPolicyAwareServiceTicketValidationAuthorizer);
        }
        
        @Bean
        @Autowired
        @ConditionalOnMissingBean(name = "authenticationPolicyAwareServiceTicketValidationAuthorizer")
        public ServiceTicketValidationAuthorizer authenticationPolicyAwareServiceTicketValidationAuthorizer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(AuthenticationEventExecutionPlan.DEFAULT_BEAN_NAME)
            final AuthenticationEventExecutionPlan authenticationEventExecutionPlan) {
            return new AuthenticationPolicyAwareServiceTicketValidationAuthorizer(servicesManager,
                authenticationEventExecutionPlan, applicationContext);
        }
    }


}
