package org.apereo.cas.validation.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.validation.Cas10ProtocolValidationSpecification;
import org.apereo.cas.validation.Cas20ProtocolValidationSpecification;
import org.apereo.cas.validation.Cas20WithoutProxyingValidationSpecification;
import org.apereo.cas.validation.CasProtocolValidationSpecification;
import org.apereo.cas.validation.DefaultServiceTicketValidationAuthorizersExecutionPlan;
import org.apereo.cas.validation.RegisteredServiceRequiredHandlersServiceTicketValidationAuthorizer;
import org.apereo.cas.validation.RequestedContextValidator;
import org.apereo.cas.validation.ServiceTicketValidationAuthorizer;
import org.apereo.cas.validation.ServiceTicketValidationAuthorizerConfigurer;
import org.apereo.cas.validation.ServiceTicketValidationAuthorizersExecutionPlan;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.List;
import java.util.Optional;

/**
 * This is {@link CasCoreValidationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreValidationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasCoreValidationConfiguration implements ServiceTicketValidationAuthorizerConfigurer {
    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Bean
    @Scope(value = "prototype")
    public CasProtocolValidationSpecification cas10ProtocolValidationSpecification() {
        return new Cas10ProtocolValidationSpecification();
    }

    @Bean
    @Scope(value = "prototype")
    public CasProtocolValidationSpecification cas20ProtocolValidationSpecification() {
        return new Cas20ProtocolValidationSpecification();
    }

    @Bean
    @Scope(value = "prototype")
    public CasProtocolValidationSpecification cas20WithoutProxyProtocolValidationSpecification() {
        return new Cas20WithoutProxyingValidationSpecification();
    }

    @Autowired
    @Bean
    @ConditionalOnMissingBean(name = "serviceValidationAuthorizers")
    public ServiceTicketValidationAuthorizersExecutionPlan serviceValidationAuthorizers(final List<ServiceTicketValidationAuthorizerConfigurer> configurers) {
        val plan = new DefaultServiceTicketValidationAuthorizersExecutionPlan();
        configurers.forEach(c -> {
            val name = RegExUtils.removePattern(c.getClass().getSimpleName(), "\\$.+");
            LOGGER.trace("Configuring service ticket validation authorizer execution plan [{}]", name);
            c.configureAuthorizersExecutionPlan(plan);
        });
        return plan;
    }

    @Bean
    public ServiceTicketValidationAuthorizer requiredHandlersServiceTicketValidationAuthorizer() {
        return new RegisteredServiceRequiredHandlersServiceTicketValidationAuthorizer(this.servicesManager.getIfAvailable());
    }

    @Override
    public void configureAuthorizersExecutionPlan(final ServiceTicketValidationAuthorizersExecutionPlan plan) {
        plan.registerAuthorizer(requiredHandlersServiceTicketValidationAuthorizer());
    }

    @Bean
    @ConditionalOnMissingBean(name = "requestedContextValidator")
    public RequestedContextValidator requestedContextValidator() {
        return (assertion, request) -> {
            LOGGER.debug("Locating the primary authentication associated with this service request [{}]", assertion.getService());
            val service = servicesManager.getIfAvailable().findServiceBy(assertion.getService());
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(assertion.getService(), service);
            return Pair.of(Boolean.TRUE, Optional.empty());
        };
    }
}
