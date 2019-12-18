package org.apereo.cas.config;

import org.apereo.cas.audit.AuditPrincipalIdProvider;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationPostProcessor;
import org.apereo.cas.authentication.SurrogateAuthenticationExpirationPolicyBuilder;
import org.apereo.cas.authentication.SurrogateAuthenticationPostProcessor;
import org.apereo.cas.authentication.SurrogatePrincipalBuilder;
import org.apereo.cas.authentication.SurrogatePrincipalElectionStrategy;
import org.apereo.cas.authentication.SurrogatePrincipalResolver;
import org.apereo.cas.authentication.audit.SurrogateAuditPrincipalIdProvider;
import org.apereo.cas.authentication.event.SurrogateAuthenticationEventListener;
import org.apereo.cas.authentication.principal.PrincipalElectionStrategyConfigurer;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolutionExecutionPlanConfigurer;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.surrogate.JsonResourceSurrogateAuthenticationService;
import org.apereo.cas.authentication.surrogate.SimpleSurrogateAuthenticationService;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.expiration.builder.TicketGrantingTicketExpirationPolicyBuilder;
import org.apereo.cas.util.io.CommunicationsManager;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This is {@link SurrogateAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @author John Gasper
 * @author Dmitriy Kopylenko
 * @since 5.1.0
 */
@Configuration("surrogateAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class SurrogateAuthenticationConfiguration {
    @Autowired
    @Qualifier("attributeRepository")
    private ObjectProvider<IPersonAttributeDao> attributeRepository;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("communicationsManager")
    private ObjectProvider<CommunicationsManager> communicationsManager;

    @Autowired
    @Qualifier("registeredServiceAccessStrategyEnforcer")
    private ObjectProvider<AuditableExecution> registeredServiceAccessStrategyEnforcer;

    @Autowired
    @Qualifier("surrogateEligibilityAuditableExecution")
    private ObjectProvider<AuditableExecution> surrogateEligibilityAuditableExecution;

    @Bean
    @RefreshScope
    public ExpirationPolicyBuilder grantingTicketExpirationPolicy() {
        val grantingTicketExpirationPolicy = new TicketGrantingTicketExpirationPolicyBuilder(casProperties);
        return new SurrogateAuthenticationExpirationPolicyBuilder(grantingTicketExpirationPolicy, casProperties);
    }

    @ConditionalOnMissingBean(name = "surrogatePrincipalFactory")
    @RefreshScope
    @Bean
    public PrincipalFactory surrogatePrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @RefreshScope
    @ConditionalOnMissingBean(name = "surrogateAuthenticationService")
    @Bean
    @SneakyThrows
    public SurrogateAuthenticationService surrogateAuthenticationService() {
        val su = casProperties.getAuthn().getSurrogate();
        if (su.getJson().getLocation() != null) {
            LOGGER.debug("Using JSON resource [{}] to locate surrogate accounts", su.getJson().getLocation());
            return new JsonResourceSurrogateAuthenticationService(su.getJson().getLocation(), servicesManager.getObject());
        }
        val accounts = new HashMap<String, List>();
        su.getSimple().getSurrogates().forEach((k, v) -> accounts.put(k, new ArrayList<>(StringUtils.commaDelimitedListToSet(v))));
        LOGGER.debug("Using accounts [{}] for surrogate authentication", accounts);
        return new SimpleSurrogateAuthenticationService(accounts, servicesManager.getObject());
    }

    @ConditionalOnMissingBean(name = "surrogateAuthenticationPostProcessor")
    @Bean
    public AuthenticationPostProcessor surrogateAuthenticationPostProcessor() {
        return new SurrogateAuthenticationPostProcessor(
            surrogateAuthenticationService(),
            servicesManager.getObject(),
            applicationContext,
            registeredServiceAccessStrategyEnforcer.getObject(),
            surrogateEligibilityAuditableExecution.getObject());
    }

    @ConditionalOnMissingBean(name = "surrogatePrincipalBuilder")
    @Bean
    public SurrogatePrincipalBuilder surrogatePrincipalBuilder() {
        return new SurrogatePrincipalBuilder(surrogatePrincipalFactory(), attributeRepository.getObject(), surrogateAuthenticationService());
    }

    @ConditionalOnMissingBean(name = "surrogatePrincipalElectionStrategyConfigurer")
    @Bean
    public PrincipalElectionStrategyConfigurer surrogatePrincipalElectionStrategyConfigurer() {
        return chain -> chain.registerElectionStrategy(new SurrogatePrincipalElectionStrategy());
    }

    @Bean
    public AuditPrincipalIdProvider surrogateAuditPrincipalIdProvider() {
        return new SurrogateAuditPrincipalIdProvider();
    }

    @ConditionalOnMissingBean(name = "surrogateAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer surrogateAuthenticationEventExecutionPlanConfigurer() {
        return plan -> plan.registerAuthenticationPostProcessor(surrogateAuthenticationPostProcessor());
    }

    @ConditionalOnMissingBean(name = "surrogateAuthenticationEventListener")
    @Bean
    public SurrogateAuthenticationEventListener surrogateAuthenticationEventListener() {
        return new SurrogateAuthenticationEventListener(communicationsManager.getObject(), casProperties);
    }

    @ConditionalOnMissingBean(name = "surrogatePrincipalResolver")
    @Bean
    @RefreshScope
    public PrincipalResolver surrogatePrincipalResolver() {
        val principal = casProperties.getAuthn().getSurrogate().getPrincipal();
        val personDirectory = casProperties.getPersonDirectory();
        val principalAttribute = org.apache.commons.lang3.StringUtils.defaultIfBlank(principal.getPrincipalAttribute(), personDirectory.getPrincipalAttribute());
        return new SurrogatePrincipalResolver(attributeRepository.getObject(),
            surrogatePrincipalFactory(),
            principal.isReturnNull() || personDirectory.isReturnNull(),
            principalAttribute,
            personDirectory.isUseExistingPrincipalId() || principal.isUseExistingPrincipalId(),
            principal.isAttributeResolutionEnabled(),
            StringUtils.commaDelimitedListToSet(principal.getActiveAttributeRepositoryIds()),
            surrogatePrincipalBuilder());
    }

    @ConditionalOnMissingBean(name = "surrogatePrincipalResolutionExecutionPlanConfigurer")
    @Bean
    public PrincipalResolutionExecutionPlanConfigurer surrogatePrincipalResolutionExecutionPlanConfigurer() {
        return plan -> plan.registerPrincipalResolver(surrogatePrincipalResolver());
    }
}
