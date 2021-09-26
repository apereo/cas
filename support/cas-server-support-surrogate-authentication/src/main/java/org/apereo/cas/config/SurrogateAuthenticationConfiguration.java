package org.apereo.cas.config;

import org.apereo.cas.audit.AuditPrincipalIdProvider;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationPostProcessor;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.MultifactorAuthenticationPrincipalResolver;
import org.apereo.cas.authentication.SurrogateAuthenticationExpirationPolicyBuilder;
import org.apereo.cas.authentication.SurrogateAuthenticationPostProcessor;
import org.apereo.cas.authentication.SurrogateMultifactorAuthenticationPrincipalResolver;
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
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.expiration.builder.TicketGrantingTicketExpirationPolicyBuilder;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
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
@Configuration(value = "surrogateAuthenticationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class SurrogateAuthenticationConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @ConditionalOnMissingBean(name = "surrogateAuthenticationService")
    @Bean
    @Autowired
    public SurrogateAuthenticationService surrogateAuthenticationService(
        @Qualifier("servicesManager")
        final ServicesManager servicesManager, final CasConfigurationProperties casProperties) throws Exception {
        val su = casProperties.getAuthn().getSurrogate();
        if (su.getJson().getLocation() != null) {
            LOGGER.debug("Using JSON resource [{}] to locate surrogate accounts", su.getJson().getLocation());
            return new JsonResourceSurrogateAuthenticationService(su.getJson().getLocation(), servicesManager);
        }
        val accounts = new HashMap<String, List>();
        su.getSimple().getSurrogates().forEach((k, v) -> accounts.put(k, new ArrayList<>(StringUtils.commaDelimitedListToSet(v))));
        LOGGER.debug("Using accounts [{}] for surrogate authentication", accounts);
        return new SimpleSurrogateAuthenticationService(accounts, servicesManager);
    }

    @ConditionalOnMissingBean(name = "surrogateAuthenticationPostProcessor")
    @Bean
    @RefreshScope
    @Autowired
    public AuthenticationPostProcessor surrogateAuthenticationPostProcessor(
        @Qualifier("surrogateAuthenticationService")
        final SurrogateAuthenticationService surrogateAuthenticationService,
        @Qualifier("servicesManager")
        final ServicesManager servicesManager,
        @Qualifier("registeredServiceAccessStrategyEnforcer")
        final AuditableExecution registeredServiceAccessStrategyEnforcer,
        @Qualifier("surrogateEligibilityAuditableExecution")
        final AuditableExecution surrogateEligibilityAuditableExecution, final ConfigurableApplicationContext applicationContext) throws Exception {
        return new SurrogateAuthenticationPostProcessor(surrogateAuthenticationService, servicesManager, applicationContext, registeredServiceAccessStrategyEnforcer,
            surrogateEligibilityAuditableExecution);
    }

    @Bean
    @ConditionalOnMissingBean(name = "surrogateMultifactorAuthenticationPrincipalResolver")
    @RefreshScope
    public MultifactorAuthenticationPrincipalResolver surrogateMultifactorAuthenticationPrincipalResolver() {
        return new SurrogateMultifactorAuthenticationPrincipalResolver();
    }

    @ConditionalOnMissingBean(name = "surrogatePrincipalElectionStrategyConfigurer")
    @Bean
    @RefreshScope
    @Autowired
    public PrincipalElectionStrategyConfigurer surrogatePrincipalElectionStrategyConfigurer(final CasConfigurationProperties casProperties) {
        return chain -> {
            val strategy = new SurrogatePrincipalElectionStrategy();
            val merger = CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger());
            strategy.setAttributeMerger(merger);
            chain.registerElectionStrategy(strategy);
        };
    }

    @Bean
    @ConditionalOnMissingBean(name = "surrogateAuditPrincipalIdProvider")
    public AuditPrincipalIdProvider surrogateAuditPrincipalIdProvider() {
        return new SurrogateAuditPrincipalIdProvider();
    }

    @ConditionalOnMissingBean(name = "surrogateAuthenticationEventExecutionPlanConfigurer")
    @Bean
    @RefreshScope
    public AuthenticationEventExecutionPlanConfigurer surrogateAuthenticationEventExecutionPlanConfigurer(
        @Qualifier("surrogateAuthenticationPostProcessor")
        final AuthenticationPostProcessor surrogateAuthenticationPostProcessor) throws Exception {
        return plan -> plan.registerAuthenticationPostProcessor(surrogateAuthenticationPostProcessor);
    }

    @Bean
    @RefreshScope
    @Autowired
    public ExpirationPolicyBuilder grantingTicketExpirationPolicy(final CasConfigurationProperties casProperties) {
        val grantingTicketExpirationPolicy = new TicketGrantingTicketExpirationPolicyBuilder(casProperties);
        return new SurrogateAuthenticationExpirationPolicyBuilder(grantingTicketExpirationPolicy, casProperties);
    }

    @ConditionalOnMissingBean(name = "surrogateAuthenticationEventListener")
    @Bean
    @Autowired
    public SurrogateAuthenticationEventListener surrogateAuthenticationEventListener(
        @Qualifier("communicationsManager")
        final CommunicationsManager communicationsManager, final CasConfigurationProperties casProperties) {
        return new SurrogateAuthenticationEventListener(communicationsManager, casProperties);
    }

    @Configuration(value = "SurrogateAuthenticationPrincipalResolutionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @DependsOn(value = PrincipalResolver.BEAN_NAME_ATTRIBUTE_REPOSITORY)
    public static class SurrogateAuthenticationPrincipalResolutionConfiguration {

        @Autowired
        private CasConfigurationProperties casProperties;

        @ConditionalOnMissingBean(name = "surrogatePrincipalBuilder")
        @Bean
        @RefreshScope
        @Autowired
        public SurrogatePrincipalBuilder surrogatePrincipalBuilder(
            @Qualifier("surrogateAuthenticationService")
            final SurrogateAuthenticationService surrogateAuthenticationService,
            @Qualifier("surrogatePrincipalFactory")
            final PrincipalFactory surrogatePrincipalFactory,
            @Qualifier(PrincipalResolver.BEAN_NAME_ATTRIBUTE_REPOSITORY)
            final IPersonAttributeDao attributeRepository) throws Exception {
            return new SurrogatePrincipalBuilder(surrogatePrincipalFactory, attributeRepository, surrogateAuthenticationService);
        }

        @ConditionalOnMissingBean(name = "surrogatePrincipalResolver")
        @Bean
        @RefreshScope
        @Autowired
        public PrincipalResolver surrogatePrincipalResolver(
            @Qualifier("surrogatePrincipalFactory")
            final PrincipalFactory surrogatePrincipalFactory,
            @Qualifier("surrogatePrincipalBuilder")
            final SurrogatePrincipalBuilder surrogatePrincipalBuilder,
            @Qualifier(PrincipalResolver.BEAN_NAME_ATTRIBUTE_REPOSITORY)
            final IPersonAttributeDao attributeRepository) {
            val principal = casProperties.getAuthn().getSurrogate().getPrincipal();
            val personDirectory = casProperties.getPersonDirectory();
            var attributeMerger = CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger());
            val resolver =
                CoreAuthenticationUtils.newPersonDirectoryPrincipalResolver(surrogatePrincipalFactory, attributeRepository, attributeMerger, SurrogatePrincipalResolver.class, principal,
                    personDirectory);
            resolver.setSurrogatePrincipalBuilder(surrogatePrincipalBuilder);
            return resolver;
        }

        @ConditionalOnMissingBean(name = "surrogatePrincipalFactory")
        @RefreshScope
        @Bean
        public PrincipalFactory surrogatePrincipalFactory() {
            return PrincipalFactoryUtils.newPrincipalFactory();
        }

        @ConditionalOnMissingBean(name = "surrogatePrincipalResolutionExecutionPlanConfigurer")
        @Bean
        @Autowired
        public PrincipalResolutionExecutionPlanConfigurer surrogatePrincipalResolutionExecutionPlanConfigurer(
            @Qualifier("surrogatePrincipalResolver")
            final PrincipalResolver surrogatePrincipalResolver) {
            return plan -> plan.registerPrincipalResolver(surrogatePrincipalResolver);
        }
    }
}
