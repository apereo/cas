package org.apereo.cas.config;

import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.attribute.AbstractAggregatingDefaultQueryPersonAttributeDao;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.attribute.AttributeRepositoryResolver;
import org.apereo.cas.authentication.attribute.MergingPersonAttributeDaoImpl;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolutionExecutionPlanConfigurer;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.principal.merger.AttributeMerger;
import org.apereo.cas.authentication.principal.resolvers.PersonDirectoryPrincipalResolver;
import org.apereo.cas.authentication.principal.resolvers.TenantPrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesCoreProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.persondir.CascadingPersonAttributeDao;
import org.apereo.cas.persondir.DefaultAttributeRepositoryResolver;
import org.apereo.cas.persondir.DefaultPersonDirectoryAttributeRepositoryPlan;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryCustomizer;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlan;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlanConfigurer;
import org.apereo.cas.persondir.cache.CachingPersonAttributeDaoImpl;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.report.CasPersonDirectoryEndpoint;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * This is {@link CasPersonDirectoryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.PersonDirectory)
@Configuration(value = "CasPersonDirectoryConfiguration", proxyBeanMethods = false)
class CasPersonDirectoryConfiguration {

    @Configuration(value = "CasPersonDirectoryPrincipalResolutionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasPersonDirectoryPrincipalResolutionConfiguration {

        @Bean
        @ConditionalOnAvailableEndpoint
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasPersonDirectoryEndpoint casPersonDirectoryEndpoint(
            @Qualifier("cachingAttributeRepository")
            final ObjectProvider<PersonAttributeDao> cachingAttributeRepository,
            @Qualifier(PersonDirectoryAttributeRepositoryPlan.BEAN_NAME)
            final ObjectProvider<PersonDirectoryAttributeRepositoryPlan> attributeRepositoryPlan,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return new CasPersonDirectoryEndpoint(casProperties, applicationContext,
                cachingAttributeRepository, attributeRepositoryPlan);
        }


        @ConditionalOnMissingBean(name = "personDirectoryPrincipalFactory")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PrincipalFactory personDirectoryPrincipalFactory() {
            return PrincipalFactoryUtils.newPrincipalFactory();
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "personDirectoryAttributeRepositoryPrincipalResolver")
        public PrincipalResolver personDirectoryAttributeRepositoryPrincipalResolver(
            @Qualifier(AttributeRepositoryResolver.BEAN_NAME)
            final AttributeRepositoryResolver attributeRepositoryResolver,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(AttributeDefinitionStore.BEAN_NAME)
            final AttributeDefinitionStore attributeDefinitionStore,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier("attributeRepositoryAttributeMerger")
            final AttributeMerger attributeRepositoryAttributeMerger,
            final CasConfigurationProperties casProperties,
            @Qualifier("personDirectoryPrincipalFactory")
            final PrincipalFactory personDirectoryPrincipalFactory,
            @Qualifier(PrincipalResolver.BEAN_NAME_ATTRIBUTE_REPOSITORY)
            final PersonAttributeDao attributeRepository) {
            val personDirectory = casProperties.getPersonDirectory();
            return PersonDirectoryPrincipalResolver.newPersonDirectoryPrincipalResolver(
                applicationContext, personDirectoryPrincipalFactory,
                attributeRepository, attributeRepositoryAttributeMerger,
                servicesManager, attributeDefinitionStore, attributeRepositoryResolver, personDirectory);
        }

        @ConditionalOnMissingBean(name = "principalResolutionExecutionPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PrincipalResolutionExecutionPlanConfigurer principalResolutionExecutionPlanConfigurer(
            @Qualifier(PersonDirectoryAttributeRepositoryPlan.BEAN_NAME)
            final PersonDirectoryAttributeRepositoryPlan personDirectoryAttributeRepositoryPlan,
            @Qualifier("personDirectoryAttributeRepositoryPrincipalResolver")
            final PrincipalResolver personDirectoryAttributeRepositoryPrincipalResolver) {
            return plan -> {
                if (personDirectoryAttributeRepositoryPlan.isEmpty()) {
                    LOGGER.debug("Attribute repository sources are not available for person-directory principal resolution");
                } else {
                    LOGGER.trace("Attribute repository sources are defined and available for person-directory principal resolution chain. ");
                    plan.registerPrincipalResolver(personDirectoryAttributeRepositoryPrincipalResolver);
                }
            };
        }
    }

    @Configuration(value = "CasPersonDirectoryAttributeRepositoryPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasPersonDirectoryAttributeRepositoryPlanConfiguration {
        @ConditionalOnMissingBean(name = PersonDirectoryAttributeRepositoryPlan.BEAN_NAME)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PersonDirectoryAttributeRepositoryPlan personDirectoryAttributeRepositoryPlan(
            final List<PersonDirectoryAttributeRepositoryPlanConfigurer> configurers,
            final ObjectProvider<List<PersonDirectoryAttributeRepositoryCustomizer>> customizers) {
            val plan = new DefaultPersonDirectoryAttributeRepositoryPlan(
                Optional.ofNullable(customizers.getIfAvailable()).orElseGet(ArrayList::new));
            configurers.forEach(cfg -> cfg.configureAttributeRepositoryPlan(plan));
            AnnotationAwareOrderComparator.sort(plan.getAttributeRepositories());
            LOGGER.trace("Final list of attribute repositories is [{}]", plan.getAttributeRepositories());
            return plan;
        }
    }

    @Configuration(value = "CasPersonDirectoryAttributeRepositoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasPersonDirectoryAttributeRepositoryConfiguration {
        private static AbstractAggregatingDefaultQueryPersonAttributeDao getAggregateAttributeRepository(
            final CasConfigurationProperties casProperties) {
            val properties = casProperties.getAuthn().getAttributeRepository();
            if (properties.getCore().getAggregation() == PrincipalAttributesCoreProperties.AggregationStrategyTypes.CASCADE) {
                val dao = new CascadingPersonAttributeDao();
                dao.setAddOriginalAttributesToQuery(true);
                dao.setStopIfFirstDaoReturnsNull(properties.getCore().isStopCascadingWhenNoInitialResults());
                return dao;
            }
            return new MergingPersonAttributeDaoImpl();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = AttributeRepositoryResolver.BEAN_NAME)
        public AttributeRepositoryResolver attributeRepositoryResolver(
            @Qualifier(TenantExtractor.BEAN_NAME)
            final TenantExtractor tenantExtractor,
            final CasConfigurationProperties casProperties,
            @Qualifier(ServicesManager.BEAN_NAME) final ServicesManager servicesManager) {
            return new DefaultAttributeRepositoryResolver(servicesManager, tenantExtractor, casProperties);
        }

        @Bean(name = {"cachingAttributeRepository", PrincipalResolver.BEAN_NAME_ATTRIBUTE_REPOSITORY})
        @ConditionalOnMissingBean(name = {"cachingAttributeRepository", PrincipalResolver.BEAN_NAME_ATTRIBUTE_REPOSITORY})
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PersonAttributeDao cachingAttributeRepository(
            final CasConfigurationProperties casProperties,
            @Qualifier("aggregatingAttributeRepository")
            final PersonAttributeDao aggregatingAttributeRepository) {
            val props = casProperties.getAuthn().getAttributeRepository().getCore();
            if (props.getExpirationTime() <= 0) {
                LOGGER.warn("Attribute repository caching is disabled");
                return aggregatingAttributeRepository;
            }

            val impl = new CachingPersonAttributeDaoImpl();
            impl.setCacheNullResults(false);
            val userinfoCache = Caffeine.newBuilder()
                .maximumSize(props.getMaximumCacheSize())
                .expireAfterWrite(props.getExpirationTime(), TimeUnit.valueOf(props.getExpirationTimeUnit().toUpperCase(Locale.ENGLISH)))
                .build();
            impl.setUserInfoCache((Map) userinfoCache.asMap());
            impl.setCachedPersonAttributesDao(aggregatingAttributeRepository);
            LOGGER.trace("Configured cache expiration policy for attribute sources to be [{}] minute(s)", props.getExpirationTime());
            return impl;
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "attributeRepositoryAttributeMerger")
        public AttributeMerger attributeRepositoryAttributeMerger(final CasConfigurationProperties casProperties) {
            return CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger());
        }

        @Bean
        @ConditionalOnMissingBean(name = "aggregatingAttributeRepository")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PersonAttributeDao aggregatingAttributeRepository(
            @Qualifier("attributeRepositoryAttributeMerger")
            final AttributeMerger attributeRepositoryAttributeMerger,
            final CasConfigurationProperties casProperties,
            @Qualifier(PersonDirectoryAttributeRepositoryPlan.BEAN_NAME)
            final PersonDirectoryAttributeRepositoryPlan personDirectoryAttributeRepositoryPlan) {
            val aggregate = getAggregateAttributeRepository(casProperties);
            aggregate.setAttributeMerger(attributeRepositoryAttributeMerger);

            val list = personDirectoryAttributeRepositoryPlan.getAttributeRepositories();
            aggregate.setPersonAttributeDaos(list);

            val properties = casProperties.getAuthn().getAttributeRepository();
            aggregate.setRequireAll(properties.getCore().isRequireAllRepositorySources());
            if (list.isEmpty()) {
                LOGGER.debug("No attribute repository sources are available/defined to merge together.");
            } else if (LOGGER.isDebugEnabled()) {
                val names = list
                    .stream()
                    .map(p -> Arrays.toString(p.getId()))
                    .collect(Collectors.joining(","));
                LOGGER.debug("Configured attribute repository sources to merge together: [{}]", names);
            }

            val recoverExceptions = properties.getCore().isRecoverExceptions();
            aggregate.setRecoverExceptions(recoverExceptions);
            LOGGER.trace("Configured attribute repository to recover from exceptions: [{}]", recoverExceptions);

            return aggregate;
        }

        @Bean
        @Lazy(false)
        public InitializingBean casPersonDirectoryInitializer(final CasConfigurationProperties casProperties) {
            return () -> FunctionUtils.doIf(LOGGER.isInfoEnabled(), value -> {
                val stub = casProperties.getAuthn().getAttributeRepository().getStub();
                val attrs = stub.getAttributes();
                if (!attrs.isEmpty()) {
                    LOGGER.info("Found and added static attributes [{}] to the list of candidate attribute repositories", attrs.keySet());
                }
            }).accept(null);
        }
    }


    @Configuration(value = "CasPersonDirectoryPrincipalResolutionMultitenancyConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasPersonDirectoryPrincipalResolutionMultitenancyConfiguration {
        @ConditionalOnMissingBean(name = "multitenancyPrincipalResolutionExecutionPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PrincipalResolutionExecutionPlanConfigurer multitenancyPrincipalResolutionExecutionPlanConfigurer(
            @Qualifier(AttributeRepositoryResolver.BEAN_NAME)
            final AttributeRepositoryResolver attributeRepositoryResolver,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(AttributeDefinitionStore.BEAN_NAME)
            final AttributeDefinitionStore attributeDefinitionStore,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier("attributeRepositoryAttributeMerger")
            final AttributeMerger attributeRepositoryAttributeMerger,
            final CasConfigurationProperties casProperties,
            @Qualifier("personDirectoryPrincipalFactory")
            final PrincipalFactory personDirectoryPrincipalFactory,
            @Qualifier(PrincipalResolver.BEAN_NAME_ATTRIBUTE_REPOSITORY)
            final PersonAttributeDao attributeRepository) {
            return plan -> {
                if (casProperties.getMultitenancy().getCore().isEnabled()) {
                    val personDirectory = casProperties.getPersonDirectory();
                    val resolver = PersonDirectoryPrincipalResolver.newPersonDirectoryPrincipalResolver(
                        applicationContext, personDirectoryPrincipalFactory,
                        attributeRepository, attributeRepositoryAttributeMerger,
                        TenantPrincipalResolver.class,
                        servicesManager, attributeDefinitionStore, attributeRepositoryResolver, personDirectory);
                    plan.registerPrincipalResolver(resolver);
                }
            };
        }
        
    }
}
