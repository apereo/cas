package org.apereo.cas.validation.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
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
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @Autowired
    @ConditionalOnMissingBean(name = "cas10ProtocolValidationSpecification")
    public CasProtocolValidationSpecification cas10ProtocolValidationSpecification(
        @Qualifier("servicesManager")
        final ServicesManager servicesManager) {
        return new Cas10ProtocolValidationSpecification(servicesManager);
    }

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @Autowired
    @ConditionalOnMissingBean(name = "cas20ProtocolValidationSpecification")
    public CasProtocolValidationSpecification cas20ProtocolValidationSpecification(
        @Qualifier("servicesManager")
        final ServicesManager servicesManager) {
        return new Cas20ProtocolValidationSpecification(servicesManager);
    }

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @ConditionalOnMissingBean(name = "cas20WithoutProxyProtocolValidationSpecification")
    @Autowired
    public CasProtocolValidationSpecification cas20WithoutProxyProtocolValidationSpecification(
        @Qualifier("servicesManager")
        final ServicesManager servicesManager) {
        return new Cas20WithoutProxyingValidationSpecification(servicesManager);
    }

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

    @Bean
    @Autowired
    @ConditionalOnMissingBean(name = "authenticationPolicyAwareServiceTicketValidationAuthorizer")
    public ServiceTicketValidationAuthorizer authenticationPolicyAwareServiceTicketValidationAuthorizer(
        @Qualifier("servicesManager")
        final ServicesManager servicesManager,
        @Qualifier(AuthenticationEventExecutionPlan.DEFAULT_BEAN_NAME)
        final AuthenticationEventExecutionPlan authenticationEventExecutionPlan) {
        return new AuthenticationPolicyAwareServiceTicketValidationAuthorizer(servicesManager, authenticationEventExecutionPlan, applicationContext);
    }

    @Bean
    @Autowired
    @ConditionalOnMissingBean(name = "casCoreServiceTicketValidationAuthorizerConfigurer")
    public ServiceTicketValidationAuthorizerConfigurer casCoreServiceTicketValidationAuthorizerConfigurer(
        @Qualifier("authenticationPolicyAwareServiceTicketValidationAuthorizer")
        final ServiceTicketValidationAuthorizer authenticationPolicyAwareServiceTicketValidationAuthorizer) {
        return plan -> plan.registerAuthorizer(authenticationPolicyAwareServiceTicketValidationAuthorizer);
    }
}
