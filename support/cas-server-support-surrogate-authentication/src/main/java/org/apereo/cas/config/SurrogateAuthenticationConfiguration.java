package org.apereo.cas.config;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationPostProcessor;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.DefaultSurrogateAuthenticationPrincipalBuilder;
import org.apereo.cas.authentication.MultifactorAuthenticationPrincipalResolver;
import org.apereo.cas.authentication.SurrogateAuthenticationExpirationPolicyBuilder;
import org.apereo.cas.authentication.SurrogateAuthenticationPostProcessor;
import org.apereo.cas.authentication.SurrogateAuthenticationPrincipalBuilder;
import org.apereo.cas.authentication.SurrogateMultifactorAuthenticationPrincipalResolver;
import org.apereo.cas.authentication.SurrogatePrincipalElectionStrategy;
import org.apereo.cas.authentication.SurrogatePrincipalResolver;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.attribute.AttributeRepositoryResolver;
import org.apereo.cas.authentication.event.DefaultSurrogateAuthenticationEventListener;
import org.apereo.cas.authentication.event.SurrogateAuthenticationEventListener;
import org.apereo.cas.authentication.principal.PrincipalElectionStrategyConfigurer;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolutionExecutionPlanConfigurer;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.principal.resolvers.PersonDirectoryPrincipalResolver;
import org.apereo.cas.authentication.surrogate.ChainingSurrogateAuthenticationService;
import org.apereo.cas.authentication.surrogate.GroovySurrogateAuthenticationService;
import org.apereo.cas.authentication.surrogate.JsonResourceSurrogateAuthenticationService;
import org.apereo.cas.authentication.surrogate.SimpleSurrogateAuthenticationService;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.services.RegisteredServicePrincipalAccessStrategyEnforcer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.ServiceTicketGeneratorAuthority;
import org.apereo.cas.ticket.SurrogateServiceTicketGeneratorAuthority;
import org.apereo.cas.ticket.expiration.builder.TicketGrantingTicketExpirationPolicyBuilder;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
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
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SurrogateAuthentication)
@Configuration(value = "SurrogateAuthenticationConfiguration", proxyBeanMethods = false)
class SurrogateAuthenticationConfiguration {
    @Configuration(value = "SurrogateAuthenticationProcessorConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SurrogateAuthenticationProcessorConfiguration {
        @ConditionalOnMissingBean(name = "surrogateAuthenticationPostProcessor")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationPostProcessor surrogateAuthenticationPostProcessor(
            @Qualifier(SurrogateAuthenticationService.BEAN_NAME)
            final SurrogateAuthenticationService surrogateAuthenticationService,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(AuditableExecution.AUDITABLE_EXECUTION_REGISTERED_SERVICE_ACCESS)
            final AuditableExecution registeredServiceAccessStrategyEnforcer,
            @Qualifier("surrogateEligibilityAuditableExecution")
            final AuditableExecution surrogateEligibilityAuditableExecution,
            final ConfigurableApplicationContext applicationContext) {
            return new SurrogateAuthenticationPostProcessor(surrogateAuthenticationService,
                servicesManager, applicationContext, registeredServiceAccessStrategyEnforcer,
                surrogateEligibilityAuditableExecution);
        }

    }

    @Configuration(value = "SurrogateAuthenticationMultifactorPrincipalResolutionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SurrogateAuthenticationMultifactorPrincipalResolutionConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "surrogateMultifactorAuthenticationPrincipalResolver")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public MultifactorAuthenticationPrincipalResolver surrogateMultifactorAuthenticationPrincipalResolver() {
            return new SurrogateMultifactorAuthenticationPrincipalResolver();
        }


    }

    @Configuration(value = "SurrogateAuthenticationExpirationPolicyConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SurrogateAuthenticationExpirationPolicyConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ExpirationPolicyBuilder grantingTicketExpirationPolicy(final CasConfigurationProperties casProperties) {
            val grantingTicketExpirationPolicy = new TicketGrantingTicketExpirationPolicyBuilder(casProperties);
            return new SurrogateAuthenticationExpirationPolicyBuilder(grantingTicketExpirationPolicy, casProperties);
        }

    }

    @Configuration(value = "SurrogateAuthenticationEventsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SurrogateAuthenticationEventsConfiguration {
        @ConditionalOnMissingBean(name = "surrogateAuthenticationEventListener")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Lazy(false)
        public SurrogateAuthenticationEventListener surrogateAuthenticationEventListener(
            @Qualifier(TenantExtractor.BEAN_NAME)
            final TenantExtractor tenantExtractor,
            @Qualifier(CommunicationsManager.BEAN_NAME)
            final CommunicationsManager communicationsManager,
            final CasConfigurationProperties casProperties) {
            return new DefaultSurrogateAuthenticationEventListener(communicationsManager, casProperties, tenantExtractor);
        }

    }

    @Configuration(value = "SurrogateAuthenticationPrincipalElectionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SurrogateAuthenticationPrincipalElectionConfiguration {
        @ConditionalOnMissingBean(name = "surrogatePrincipalElectionStrategyConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PrincipalElectionStrategyConfigurer surrogatePrincipalElectionStrategyConfigurer(
            final CasConfigurationProperties casProperties) {
            return chain -> {
                val strategy = new SurrogatePrincipalElectionStrategy();
                val merger = CoreAuthenticationUtils.getAttributeMerger(
                    casProperties.getAuthn().getAttributeRepository().getCore().getMerger());
                strategy.setAttributeMerger(merger);
                chain.registerElectionStrategy(strategy);
            };
        }

    }

    @Configuration(value = "SurrogateAuthenticationServiceConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SurrogateAuthenticationServiceConfiguration {

        @ConditionalOnMissingBean(name = "surrogateServiceTicketGeneratorAuthority")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ServiceTicketGeneratorAuthority surrogateServiceTicketGeneratorAuthority(
            @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
            final PrincipalResolver defaultPrincipalResolver,
            @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
            final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies,
            @Qualifier(SurrogateAuthenticationService.BEAN_NAME)
            final SurrogateAuthenticationService surrogateAuthenticationService) {
            return new SurrogateServiceTicketGeneratorAuthority(
                surrogateAuthenticationService, authenticationRequestServiceSelectionStrategies, defaultPrincipalResolver);
        }
        
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = SurrogateAuthenticationService.BEAN_NAME)
        @Bean
        public SurrogateAuthenticationService surrogateAuthenticationService(
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            final List<BeanSupplier<SurrogateAuthenticationService>> surrogateServiceSuppliers) {
            val allServices = surrogateServiceSuppliers
                .stream()
                .map(BeanSupplier::get)
                .filter(BeanSupplier::isNotProxy)
                .sorted(AnnotationAwareOrderComparator.INSTANCE)
                .toList();
            return new ChainingSurrogateAuthenticationService(allServices, servicesManager);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "groovySurrogateAuthenticationService")
        public BeanSupplier<SurrogateAuthenticationService> groovySurrogateAuthenticationService(
            @Qualifier(RegisteredServicePrincipalAccessStrategyEnforcer.BEAN_NAME)
            final RegisteredServicePrincipalAccessStrategyEnforcer principalAccessStrategyEnforcer,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            final CasConfigurationProperties casProperties) {

            return BeanSupplier.of(SurrogateAuthenticationService.class)
                .when(() -> casProperties.getAuthn().getSurrogate().getGroovy().getLocation() != null)
                .supply(Unchecked.supplier(() -> {
                    val su = casProperties.getAuthn().getSurrogate();
                    LOGGER.debug("Using Groovy resource [{}] to locate surrogate accounts", su.getGroovy().getLocation());
                    return new GroovySurrogateAuthenticationService(servicesManager, casProperties, principalAccessStrategyEnforcer, applicationContext);
                }))
                .otherwiseNull();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "jsonSurrogateAuthenticationService")
        public BeanSupplier<SurrogateAuthenticationService> jsonSurrogateAuthenticationService(
            @Qualifier(RegisteredServicePrincipalAccessStrategyEnforcer.BEAN_NAME)
            final RegisteredServicePrincipalAccessStrategyEnforcer principalAccessStrategyEnforcer,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            final CasConfigurationProperties casProperties) {

            return BeanSupplier.of(SurrogateAuthenticationService.class)
                .when(() -> casProperties.getAuthn().getSurrogate().getJson().getLocation() != null)
                .supply(Unchecked.supplier(() -> {
                    val su = casProperties.getAuthn().getSurrogate();
                    LOGGER.debug("Using JSON resource [{}] to locate surrogate accounts", su.getJson().getLocation());
                    return new JsonResourceSurrogateAuthenticationService(servicesManager, casProperties, principalAccessStrategyEnforcer, applicationContext);
                }))
                .otherwiseNull();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "simpleSurrogateAuthenticationService")
        public BeanSupplier<SurrogateAuthenticationService> simpleSurrogateAuthenticationService(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(RegisteredServicePrincipalAccessStrategyEnforcer.BEAN_NAME)
            final RegisteredServicePrincipalAccessStrategyEnforcer principalAccessStrategyEnforcer,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(SurrogateAuthenticationService.class)
                .alwaysMatch()
                .supply(() -> {
                    val su = casProperties.getAuthn().getSurrogate();
                    val accounts = new HashMap<String, List>();
                    su.getSimple().getSurrogates().forEach((user, v) -> accounts.put(user, new ArrayList<>(StringUtils.commaDelimitedListToSet(v))));
                    LOGGER.debug("Using accounts [{}] for surrogate authentication", accounts);
                    return new SimpleSurrogateAuthenticationService(accounts, servicesManager,
                        casProperties, principalAccessStrategyEnforcer, applicationContext);
                });
        }
    }

    @Configuration(value = "SurrogateAuthenticationPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SurrogateAuthenticationPlanConfiguration {
        @ConditionalOnMissingBean(name = "surrogateAuthenticationEventExecutionPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationEventExecutionPlanConfigurer surrogateAuthenticationEventExecutionPlanConfigurer(
            @Qualifier("surrogateAuthenticationPostProcessor")
            final AuthenticationPostProcessor surrogateAuthenticationPostProcessor) {
            return plan -> plan.registerAuthenticationPostProcessor(surrogateAuthenticationPostProcessor);
        }

    }

    @Configuration(value = "SurrogateAuthenticationPrincipalBuilderConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SurrogateAuthenticationPrincipalBuilderConfiguration {
        @ConditionalOnMissingBean(name = SurrogateAuthenticationPrincipalBuilder.BEAN_NAME)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SurrogateAuthenticationPrincipalBuilder surrogatePrincipalBuilder(
            @Qualifier(AttributeRepositoryResolver.BEAN_NAME)
            final AttributeRepositoryResolver attributeRepositoryResolver,
            final CasConfigurationProperties casProperties,
            @Qualifier(SurrogateAuthenticationService.BEAN_NAME)
            final SurrogateAuthenticationService surrogateAuthenticationService,
            @Qualifier("surrogatePrincipalFactory")
            final PrincipalFactory surrogatePrincipalFactory,
            @Qualifier(PrincipalResolver.BEAN_NAME_ATTRIBUTE_REPOSITORY)
            final PersonAttributeDao attributeRepository) {
            return new DefaultSurrogateAuthenticationPrincipalBuilder(surrogatePrincipalFactory,
                attributeRepository, surrogateAuthenticationService, attributeRepositoryResolver, casProperties);
        }
    }

    @Configuration(value = "SurrogateAuthenticationPrincipalResolutionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @DependsOn(PrincipalResolver.BEAN_NAME_ATTRIBUTE_REPOSITORY)
    static class SurrogateAuthenticationPrincipalResolutionConfiguration {

        @ConditionalOnMissingBean(name = "surrogatePrincipalResolver")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PrincipalResolver surrogatePrincipalResolver(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(AttributeDefinitionStore.BEAN_NAME)
            final AttributeDefinitionStore attributeDefinitionStore,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            final CasConfigurationProperties casProperties,
            @Qualifier("surrogatePrincipalFactory")
            final PrincipalFactory surrogatePrincipalFactory,
            @Qualifier(SurrogateAuthenticationPrincipalBuilder.BEAN_NAME)
            final SurrogateAuthenticationPrincipalBuilder surrogatePrincipalBuilder,
            @Qualifier(PrincipalResolver.BEAN_NAME_ATTRIBUTE_REPOSITORY)
            final PersonAttributeDao attributeRepository,
            @Qualifier(AttributeRepositoryResolver.BEAN_NAME)
            final AttributeRepositoryResolver attributeRepositoryResolver) {
            val principal = casProperties.getAuthn().getSurrogate().getPrincipal();
            val personDirectory = casProperties.getPersonDirectory();
            val attributeMerger = CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger());
            val resolver = PersonDirectoryPrincipalResolver.newPersonDirectoryPrincipalResolver(
                applicationContext, surrogatePrincipalFactory,
                attributeRepository, attributeMerger, SurrogatePrincipalResolver.class,
                servicesManager, attributeDefinitionStore, attributeRepositoryResolver,
                principal, personDirectory);
            resolver.setSurrogatePrincipalBuilder(surrogatePrincipalBuilder);
            return resolver;
        }

    }

    @Configuration(value = "SurrogateAuthenticationPrincipalFactoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SurrogateAuthenticationPrincipalFactoryConfiguration {

        @ConditionalOnMissingBean(name = "surrogatePrincipalFactory")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public PrincipalFactory surrogatePrincipalFactory() {
            return PrincipalFactoryUtils.newPrincipalFactory();
        }
    }

    @Configuration(value = "SurrogateAuthenticationPrincipalPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SurrogateAuthenticationPrincipalPlanConfiguration {

        @ConditionalOnMissingBean(name = "surrogatePrincipalResolutionExecutionPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PrincipalResolutionExecutionPlanConfigurer surrogatePrincipalResolutionExecutionPlanConfigurer(
            @Qualifier("surrogatePrincipalResolver")
            final PrincipalResolver surrogatePrincipalResolver) {
            return plan -> plan.registerPrincipalResolver(surrogatePrincipalResolver);
        }
    }
}
