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
import org.apereo.cas.util.spring.CasEventListener;

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
import org.springframework.context.annotation.ScopedProxyMode;
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

    @Configuration(value = "SurrogateAuthenticationProcessorConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SurrogateAuthenticationProcessorConfiguration {
        @ConditionalOnMissingBean(name = "surrogateAuthenticationPostProcessor")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public AuthenticationPostProcessor surrogateAuthenticationPostProcessor(
            @Qualifier("surrogateAuthenticationService")
            final SurrogateAuthenticationService surrogateAuthenticationService,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier("registeredServiceAccessStrategyEnforcer")
            final AuditableExecution registeredServiceAccessStrategyEnforcer,
            @Qualifier("surrogateEligibilityAuditableExecution")
            final AuditableExecution surrogateEligibilityAuditableExecution, final ConfigurableApplicationContext applicationContext) throws Exception {
            return new SurrogateAuthenticationPostProcessor(surrogateAuthenticationService,
                servicesManager, applicationContext, registeredServiceAccessStrategyEnforcer,
                surrogateEligibilityAuditableExecution);
        }

    }

    @Configuration(value = "SurrogateAuthenticationMultifactorPrincipalResolutionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SurrogateAuthenticationMultifactorPrincipalResolutionConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "surrogateMultifactorAuthenticationPrincipalResolver")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public MultifactorAuthenticationPrincipalResolver surrogateMultifactorAuthenticationPrincipalResolver() {
            return new SurrogateMultifactorAuthenticationPrincipalResolver();
        }


    }

    @Configuration(value = "SurrogateAuthenticationExpirationPolicyConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SurrogateAuthenticationExpirationPolicyConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public ExpirationPolicyBuilder grantingTicketExpirationPolicy(final CasConfigurationProperties casProperties) {
            val grantingTicketExpirationPolicy = new TicketGrantingTicketExpirationPolicyBuilder(casProperties);
            return new SurrogateAuthenticationExpirationPolicyBuilder(grantingTicketExpirationPolicy, casProperties);
        }

    }

    @Configuration(value = "SurrogateAuthenticationEventsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SurrogateAuthenticationEventsConfiguration {
        @ConditionalOnMissingBean(name = "surrogateAuthenticationEventListener")
        @Bean
        @Autowired
        public CasEventListener surrogateAuthenticationEventListener(
            @Qualifier("communicationsManager")
            final CommunicationsManager communicationsManager,
            final CasConfigurationProperties casProperties) {
            return new SurrogateAuthenticationEventListener(communicationsManager, casProperties);
        }

    }

    @Configuration(value = "SurrogateAuthenticationAuditConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SurrogateAuthenticationAuditConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "surrogateAuditPrincipalIdProvider")
        public AuditPrincipalIdProvider surrogateAuditPrincipalIdProvider() {
            return new SurrogateAuditPrincipalIdProvider();
        }

    }

    @Configuration(value = "SurrogateAuthenticationPrincipalElectionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SurrogateAuthenticationPrincipalElectionConfiguration {
        @ConditionalOnMissingBean(name = "surrogatePrincipalElectionStrategyConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public PrincipalElectionStrategyConfigurer surrogatePrincipalElectionStrategyConfigurer(final CasConfigurationProperties casProperties) {
            return chain -> {
                val strategy = new SurrogatePrincipalElectionStrategy();
                val merger = CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger());
                strategy.setAttributeMerger(merger);
                chain.registerElectionStrategy(strategy);
            };
        }

    }

    @Configuration(value = "SurrogateAuthenticationServiceConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SurrogateAuthenticationServiceConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "surrogateAuthenticationService")
        @Bean
        @Autowired
        public SurrogateAuthenticationService surrogateAuthenticationService(
            @Qualifier(ServicesManager.BEAN_NAME)
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

    }

    @Configuration(value = "SurrogateAuthenticationPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SurrogateAuthenticationPlanConfiguration {
        @ConditionalOnMissingBean(name = "surrogateAuthenticationEventExecutionPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationEventExecutionPlanConfigurer surrogateAuthenticationEventExecutionPlanConfigurer(
            @Qualifier("surrogateAuthenticationPostProcessor")
            final AuthenticationPostProcessor surrogateAuthenticationPostProcessor) throws Exception {
            return plan -> plan.registerAuthenticationPostProcessor(surrogateAuthenticationPostProcessor);
        }

    }

    @Configuration(value = "SurrogateAuthenticationPrincipalBuilderConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SurrogateAuthenticationPrincipalBuilderConfiguration {

        @ConditionalOnMissingBean(name = "surrogatePrincipalBuilder")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
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
    }

    @Configuration(value = "SurrogateAuthenticationPrincipalResolutionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @DependsOn(value = PrincipalResolver.BEAN_NAME_ATTRIBUTE_REPOSITORY)
    public static class SurrogateAuthenticationPrincipalResolutionConfiguration {

        @ConditionalOnMissingBean(name = "surrogatePrincipalResolver")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public PrincipalResolver surrogatePrincipalResolver(
            final CasConfigurationProperties casProperties,
            @Qualifier("surrogatePrincipalFactory")
            final PrincipalFactory surrogatePrincipalFactory,
            @Qualifier("surrogatePrincipalBuilder")
            final SurrogatePrincipalBuilder surrogatePrincipalBuilder,
            @Qualifier(PrincipalResolver.BEAN_NAME_ATTRIBUTE_REPOSITORY)
            final IPersonAttributeDao attributeRepository) {
            val principal = casProperties.getAuthn().getSurrogate().getPrincipal();
            val personDirectory = casProperties.getPersonDirectory();
            val attributeMerger = CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger());
            val resolver = CoreAuthenticationUtils.newPersonDirectoryPrincipalResolver(surrogatePrincipalFactory,
                attributeRepository, attributeMerger, SurrogatePrincipalResolver.class, principal,
                personDirectory);
            resolver.setSurrogatePrincipalBuilder(surrogatePrincipalBuilder);
            return resolver;
        }

    }

    @Configuration(value = "SurrogateAuthenticationPrincipalFactoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SurrogateAuthenticationPrincipalFactoryConfiguration {

        @ConditionalOnMissingBean(name = "surrogatePrincipalFactory")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public PrincipalFactory surrogatePrincipalFactory() {
            return PrincipalFactoryUtils.newPrincipalFactory();
        }
    }

    @Configuration(value = "SurrogateAuthenticationPrincipalPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SurrogateAuthenticationPrincipalPlanConfiguration {

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
