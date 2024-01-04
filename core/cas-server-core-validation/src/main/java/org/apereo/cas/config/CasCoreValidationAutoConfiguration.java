package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.proxy.ProxyHandler;
import org.apereo.cas.ticket.proxy.support.Cas10ProxyHandler;
import org.apereo.cas.ticket.proxy.support.Cas20ProxyHandler;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.validation.AbstractCasProtocolValidationSpecification;
import org.apereo.cas.validation.AuthenticationPolicyAwareServiceTicketValidationAuthorizer;
import org.apereo.cas.validation.CasProtocolValidationSpecification;
import org.apereo.cas.validation.DefaultCasProtocolValidationSpecification;
import org.apereo.cas.validation.DefaultServiceTicketValidationAuthorizersExecutionPlan;
import org.apereo.cas.validation.ServiceTicketValidationAuthorizer;
import org.apereo.cas.validation.ServiceTicketValidationAuthorizerConfigurer;
import org.apereo.cas.validation.ServiceTicketValidationAuthorizersExecutionPlan;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

import java.util.List;

/**
 * This is {@link CasCoreValidationAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Validation)
@AutoConfiguration
public class CasCoreValidationAutoConfiguration {
    private static final BeanCondition CONDITION_PROXY_AUTHN = BeanCondition.on("cas.sso.proxy-authn-enabled").isTrue().evenIfMissing();

    @Configuration(value = "CasCoreValidationProxyConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreValidationProxyConfiguration {
        @ConditionalOnMissingBean(name = "proxy10Handler")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ProxyHandler proxy10Handler(final ConfigurableApplicationContext applicationContext) throws Exception {
            return BeanSupplier.of(ProxyHandler.class)
                .when(CONDITION_PROXY_AUTHN.given(applicationContext.getEnvironment()))
                .supply(Cas10ProxyHandler::new)
                .otherwiseProxy()
                .get();
        }

        @ConditionalOnMissingBean(name = "proxy20Handler")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ProxyHandler proxy20Handler(
            @Qualifier("proxy20TicketUniqueIdGenerator") final UniqueTicketIdGenerator proxy20TicketUniqueIdGenerator,
            @Qualifier(HttpClient.BEAN_NAME_HTTPCLIENT_TRUST_STORE) final HttpClient httpClient,
            final ConfigurableApplicationContext applicationContext) throws Exception {
            return BeanSupplier.of(ProxyHandler.class)
                .when(CONDITION_PROXY_AUTHN.given(applicationContext.getEnvironment()))
                .supply(() -> new Cas20ProxyHandler(httpClient, proxy20TicketUniqueIdGenerator))
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "CasCoreValidationSpecificationConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreValidationSpecificationConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        @ConditionalOnMissingBean(name = "casSingleAuthenticationProtocolValidationSpecification")
        public CasProtocolValidationSpecification casSingleAuthenticationProtocolValidationSpecification(
            @Qualifier(ServicesManager.BEAN_NAME) final ServicesManager servicesManager) {
            return new DefaultCasProtocolValidationSpecification(servicesManager,
                AbstractCasProtocolValidationSpecification.ASSERTION_SINGLE_AUTHENTICATION);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        @ConditionalOnMissingBean(name = "casAlwaysSatisfiedProtocolValidationSpecification")
        public CasProtocolValidationSpecification casAlwaysSatisfiedProtocolValidationSpecification(
            @Qualifier(ServicesManager.BEAN_NAME) final ServicesManager servicesManager) {
            return new DefaultCasProtocolValidationSpecification(servicesManager,
                AbstractCasProtocolValidationSpecification.ASSERTION_ALWAYS_SATISFIED);
        }
    }

    @Configuration(value = "CasCoreValidationExecutionPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreValidationExecutionPlanConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "serviceValidationAuthorizers")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ServiceTicketValidationAuthorizersExecutionPlan serviceValidationAuthorizers(
            final List<ServiceTicketValidationAuthorizerConfigurer> configurers) {
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
    static class CasCoreValidationAuthorizerConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "casCoreServiceTicketValidationAuthorizerConfigurer")
        public ServiceTicketValidationAuthorizerConfigurer casCoreServiceTicketValidationAuthorizerConfigurer(
            @Qualifier("authenticationPolicyAwareServiceTicketValidationAuthorizer") final ServiceTicketValidationAuthorizer authenticationPolicyAwareServiceTicketValidationAuthorizer) {
            return plan -> plan.registerAuthorizer(authenticationPolicyAwareServiceTicketValidationAuthorizer);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "authenticationPolicyAwareServiceTicketValidationAuthorizer")
        public ServiceTicketValidationAuthorizer authenticationPolicyAwareServiceTicketValidationAuthorizer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(ServicesManager.BEAN_NAME) final ServicesManager servicesManager,
            @Qualifier(AuthenticationEventExecutionPlan.DEFAULT_BEAN_NAME) final AuthenticationEventExecutionPlan authenticationEventExecutionPlan) {
            return new AuthenticationPolicyAwareServiceTicketValidationAuthorizer(servicesManager,
                authenticationEventExecutionPlan, applicationContext);
        }
    }

}
