package org.apereo.cas.config;

import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.PrincipalElectionStrategy;
import org.apereo.cas.authentication.PrincipalElectionStrategyConflictResolver;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStoreConfigurer;
import org.apereo.cas.authentication.attribute.DefaultAttributeDefinitionStore;
import org.apereo.cas.authentication.principal.ChainingPrincipalElectionStrategy;
import org.apereo.cas.authentication.principal.DefaultPrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.DefaultPrincipalElectionStrategy;
import org.apereo.cas.authentication.principal.DefaultPrincipalResolutionExecutionPlan;
import org.apereo.cas.authentication.principal.PrincipalElectionStrategyConfigurer;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolutionExecutionPlanConfigurer;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.RegisteredServicePrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.cache.CachingPrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.merger.AttributeMerger;
import org.apereo.cas.authentication.principal.resolvers.ChainingPrincipalResolver;
import org.apereo.cas.authentication.principal.resolvers.EchoingPrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.multitenancy.TenantExtractor;
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
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * This is {@link CasCoreAuthenticationPrincipalConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Authentication)
@Configuration(value = "CasCoreAuthenticationPrincipalConfiguration", proxyBeanMethods = false)
class CasCoreAuthenticationPrincipalConfiguration {
    @Configuration(value = "CasCoreAuthenticationPrincipalResolutionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreAuthenticationPrincipalResolutionConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PrincipalResolver defaultPrincipalResolver(
            @Qualifier(TenantExtractor.BEAN_NAME)
            final TenantExtractor tenantExtractor,
            final List<PrincipalResolutionExecutionPlanConfigurer> configurers,
            final CasConfigurationProperties casProperties,
            @Qualifier(PrincipalElectionStrategy.BEAN_NAME)
            final PrincipalElectionStrategy principalElectionStrategy) {
            val plan = new DefaultPrincipalResolutionExecutionPlan();
            val sortedConfigurers = new ArrayList<>(configurers);
            AnnotationAwareOrderComparator.sortIfNecessary(sortedConfigurers);
            sortedConfigurers.forEach(Unchecked.consumer(cfg -> {
                LOGGER.trace("Configuring principal resolution execution plan [{}]", cfg.getName());
                cfg.configurePrincipalResolutionExecutionPlan(plan);
            }));
            plan.registerPrincipalResolver(new EchoingPrincipalResolver());
            val registeredPrincipalResolvers = plan.getRegisteredPrincipalResolvers();
            return new ChainingPrincipalResolver(principalElectionStrategy, tenantExtractor,
                registeredPrincipalResolvers, casProperties);
        }
    }

    @Configuration(value = "CasCoreAuthenticationPrincipalElectionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreAuthenticationPrincipalElectionConfiguration {
        @ConditionalOnMissingBean(name = PrincipalElectionStrategy.BEAN_NAME)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PrincipalElectionStrategy principalElectionStrategy(
            final List<PrincipalElectionStrategyConfigurer> configurers,
            @Qualifier("principalElectionAttributeMerger")
            final AttributeMerger attributeMerger) {
            LOGGER.trace("Building principal election strategies from [{}]", configurers);
            val chain = new ChainingPrincipalElectionStrategy();
            chain.setAttributeMerger(attributeMerger);
            AnnotationAwareOrderComparator.sortIfNecessary(configurers);

            configurers.forEach(c -> {
                LOGGER.trace("Configuring principal selection strategy: [{}]", c);
                c.configurePrincipalElectionStrategy(chain);
            });
            return chain;
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "principalElectionAttributeMerger")
        public AttributeMerger principalElectionAttributeMerger(final CasConfigurationProperties casProperties) {
            return CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger());
        }

        @ConditionalOnMissingBean(name = PrincipalElectionStrategyConflictResolver.BEAN_NAME)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PrincipalElectionStrategyConflictResolver defaultPrincipalElectionStrategyConflictResolver(
            final CasConfigurationProperties casProperties) {
            return CoreAuthenticationUtils.newPrincipalElectionStrategyConflictResolver(casProperties.getPersonDirectory());
        }

        @ConditionalOnMissingBean(name = "defaultPrincipalElectionStrategyConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PrincipalElectionStrategyConfigurer defaultPrincipalElectionStrategyConfigurer(
            @Qualifier(PrincipalElectionStrategyConflictResolver.BEAN_NAME)
            final PrincipalElectionStrategyConflictResolver defaultPrincipalElectionStrategyConflictResolver,
            @Qualifier("principalElectionAttributeMerger")
            final AttributeMerger attributeMerger,
            final CasConfigurationProperties casProperties,
            @Qualifier(PrincipalFactory.BEAN_NAME)
            final PrincipalFactory principalFactory) {
            return chain -> {
                val strategy = new DefaultPrincipalElectionStrategy(principalFactory, defaultPrincipalElectionStrategyConflictResolver);
                strategy.setAttributeMerger(attributeMerger);
                chain.registerElectionStrategy(strategy);
            };
        }
    }

    @Configuration(value = "CasCoreAuthenticationPrincipalFactoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreAuthenticationPrincipalFactoryConfiguration {

        @ConditionalOnMissingBean(name = PrincipalFactory.BEAN_NAME)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PrincipalFactory principalFactory() {
            return PrincipalFactoryUtils.newPrincipalFactory();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = PrincipalResolver.BEAN_NAME_GLOBAL_PRINCIPAL_ATTRIBUTE_REPOSITORY)
        public RegisteredServicePrincipalAttributesRepository globalPrincipalAttributeRepository(final CasConfigurationProperties casProperties) {
            val props = casProperties.getAuthn().getAttributeRepository().getCore();
            val cacheTime = props.getExpirationTime();
            if (cacheTime <= 0) {
                LOGGER.warn("Caching for the global principal attribute repository is disabled");
                return new DefaultPrincipalAttributesRepository();
            }
            return new CachingPrincipalAttributesRepository(props.getExpirationTimeUnit().toUpperCase(Locale.ENGLISH), cacheTime);
        }

    }

    @Configuration(value = "CasCoreAuthenticationAttributeDefinitionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreAuthenticationAttributeDefinitionConfiguration {
        @ConditionalOnMissingBean(name = AttributeDefinitionStore.BEAN_NAME)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AttributeDefinitionStore attributeDefinitionStore(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) throws Exception {
            val builders = applicationContext.getBeansOfType(AttributeDefinitionStoreConfigurer.class).values();
            val store = new DefaultAttributeDefinitionStore();
            store.setScope(casProperties.getServer().getScope());
            builders
                .stream()
                .filter(BeanSupplier::isNotProxy)
                .sorted(AnnotationAwareOrderComparator.INSTANCE)
                .map(AttributeDefinitionStoreConfigurer::load)
                .forEach(store::registerAttributeDefinitions);
            val resource = casProperties.getAuthn().getAttributeRepository().getAttributeDefinitionStore().getJson().getLocation();
            store.importStore(resource);
            store.watchStore(resource);
            return store;
        }
    }

}
