package org.apereo.cas.config;

import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.attribute.DefaultAttributeDefinitionStore;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolutionExecutionPlanConfigurer;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.persondir.DefaultPersonDirectoryAttributeRepositoryPlan;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryCustomizer;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlan;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlanConfigurer;
import org.apereo.cas.util.function.FunctionUtils;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.support.AbstractAggregatingDefaultQueryPersonAttributeDao;
import org.apereo.services.persondir.support.CachingPersonAttributeDaoImpl;
import org.apereo.services.persondir.support.CascadingPersonAttributeDao;
import org.apereo.services.persondir.support.MergingPersonAttributeDaoImpl;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
@Configuration(value = "casPersonDirectoryConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasPersonDirectoryConfiguration {

    @Configuration(value = "CasPersonDirectoryAttributeDefinitionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasPersonDirectoryAttributeDefinitionConfiguration {

        @ConditionalOnMissingBean(name = AttributeDefinitionStore.BEAN_NAME)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public AttributeDefinitionStore attributeDefinitionStore(final CasConfigurationProperties casProperties) throws Exception {
            val resource = casProperties.getAuthn().getAttributeRepository().getAttributeDefinitionStore().getJson().getLocation();
            val store = new DefaultAttributeDefinitionStore(resource);
            store.setScope(casProperties.getServer().getScope());
            return store;
        }

    }

    @Configuration(value = "CasPersonDirectoryPrincipalResolutionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasPersonDirectoryPrincipalResolutionConfiguration {
        @ConditionalOnMissingBean(name = "personDirectoryPrincipalFactory")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PrincipalFactory personDirectoryPrincipalFactory() {
            return PrincipalFactoryUtils.newPrincipalFactory();
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @Autowired
        @ConditionalOnMissingBean(name = "personDirectoryAttributeRepositoryPrincipalResolver")
        public PrincipalResolver personDirectoryAttributeRepositoryPrincipalResolver(
            final CasConfigurationProperties casProperties,
            @Qualifier("personDirectoryPrincipalFactory")
            final PrincipalFactory personDirectoryPrincipalFactory,
            @Qualifier(PrincipalResolver.BEAN_NAME_ATTRIBUTE_REPOSITORY)
            final IPersonAttributeDao attributeRepository) {
            val personDirectory = casProperties.getPersonDirectory();
            val attributeMerger = CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger());
            return CoreAuthenticationUtils.newPersonDirectoryPrincipalResolver(personDirectoryPrincipalFactory,
                attributeRepository, attributeMerger, personDirectory);
        }

        @ConditionalOnMissingBean(name = "principalResolutionExecutionPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public PrincipalResolutionExecutionPlanConfigurer principalResolutionExecutionPlanConfigurer(
            @Qualifier("personDirectoryAttributeRepositoryPlan")
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
    public static class CasPersonDirectoryAttributeRepositoryPlanConfiguration {
        @ConditionalOnMissingBean(name = "personDirectoryAttributeRepositoryPlan")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public PersonDirectoryAttributeRepositoryPlan personDirectoryAttributeRepositoryPlan(
            final List<PersonDirectoryAttributeRepositoryPlanConfigurer> configurers,
            final ObjectProvider<List<PersonDirectoryAttributeRepositoryCustomizer>> customizers) {
            val plan = new DefaultPersonDirectoryAttributeRepositoryPlan(
                Optional.ofNullable(customizers.getIfAvailable()).orElse(new ArrayList<>()));
            configurers.forEach(c -> c.configureAttributeRepositoryPlan(plan));
            AnnotationAwareOrderComparator.sort(plan.getAttributeRepositories());
            LOGGER.trace("Final list of attribute repositories is [{}]", plan.getAttributeRepositories());
            return plan;
        }
    }

    @Configuration(value = "CasPersonDirectoryAttributeRepositoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasPersonDirectoryAttributeRepositoryConfiguration {
        private static AbstractAggregatingDefaultQueryPersonAttributeDao getAggregateAttributeRepository(
            final CasConfigurationProperties casProperties) {
            val properties = casProperties.getAuthn().getAttributeRepository();
            switch (properties.getCore().getAggregation()) {
                case CASCADE:
                    val dao = new CascadingPersonAttributeDao();
                    dao.setAddOriginalAttributesToQuery(true);
                    dao.setStopIfFirstDaoReturnsNull(true);
                    return dao;
                case MERGE:
                default:
                    return new MergingPersonAttributeDaoImpl();
            }
        }


        @Bean(name = {"cachingAttributeRepository", PrincipalResolver.BEAN_NAME_ATTRIBUTE_REPOSITORY})
        @ConditionalOnMissingBean(name = {"cachingAttributeRepository", PrincipalResolver.BEAN_NAME_ATTRIBUTE_REPOSITORY})
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public IPersonAttributeDao cachingAttributeRepository(
            final CasConfigurationProperties casProperties,
            @Qualifier("aggregatingAttributeRepository")
            final IPersonAttributeDao aggregatingAttributeRepository) {
            val props = casProperties.getAuthn().getAttributeRepository().getCore();
            if (props.getExpirationTime() <= 0) {
                LOGGER.warn("Attribute repository caching is disabled");
                return aggregatingAttributeRepository;
            }

            val impl = new CachingPersonAttributeDaoImpl();
            impl.setCacheNullResults(false);
            val userinfoCache = Caffeine.newBuilder()
                .maximumSize(props.getMaximumCacheSize())
                .expireAfterWrite(props.getExpirationTime(), TimeUnit.valueOf(props.getExpirationTimeUnit().toUpperCase()))
                .build();
            impl.setUserInfoCache((Map) userinfoCache.asMap());
            impl.setCachedPersonAttributesDao(aggregatingAttributeRepository);
            LOGGER.trace("Configured cache expiration policy for attribute sources to be [{}] minute(s)", props.getExpirationTime());
            return impl;
        }

        @Bean
        @ConditionalOnMissingBean(name = "aggregatingAttributeRepository")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public IPersonAttributeDao aggregatingAttributeRepository(
            final CasConfigurationProperties casProperties,
            @Qualifier("personDirectoryAttributeRepositoryPlan")
            final PersonDirectoryAttributeRepositoryPlan personDirectoryAttributeRepositoryPlan) {
            val aggregate = getAggregateAttributeRepository(casProperties);

            val properties = casProperties.getAuthn().getAttributeRepository();
            val attributeMerger = CoreAuthenticationUtils.getAttributeMerger(properties.getCore().getMerger());
            LOGGER.trace("Configured merging strategy for attribute sources is [{}]", attributeMerger);
            aggregate.setMerger(attributeMerger);

            val list = personDirectoryAttributeRepositoryPlan.getAttributeRepositories();
            aggregate.setPersonAttributeDaos(list);

            aggregate.setRequireAll(properties.getCore().isRequireAllRepositorySources());
            if (list.isEmpty()) {
                LOGGER.debug("No attribute repository sources are available/defined to merge together.");
            } else {
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
        @Autowired
        public InitializingBean casPersonDirectoryInitializer(final CasConfigurationProperties casProperties) {
            return () -> {
                FunctionUtils.doIf(LOGGER.isInfoEnabled(), value -> {
                    val stub = casProperties.getAuthn().getAttributeRepository().getStub();
                    val attrs = stub.getAttributes();
                    if (!attrs.isEmpty()) {
                        LOGGER.info("Found and added static attributes [{}] to the list of candidate attribute repositories", attrs.keySet());
                    }
                }).accept(null);
            };
        }
    }

    @Configuration(value = "CasPersonDirectoryStaticSubAttributeRepositoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasPersonDirectoryStaticSubAttributeRepositoryConfiguration {
        @ConditionalOnMissingBean(name = "stubAttributeRepositories")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public List<IPersonAttributeDao> stubAttributeRepositories(final CasConfigurationProperties casProperties) {
            val list = new ArrayList<IPersonAttributeDao>();
            val stub = casProperties.getAuthn().getAttributeRepository().getStub();
            val attrs = stub.getAttributes();
            if (!attrs.isEmpty()) {
                val dao = Beans.newStubAttributeRepository(casProperties.getAuthn().getAttributeRepository());
                list.add(dao);
            }
            return list;
        }

        @Bean
        @Autowired
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PersonDirectoryAttributeRepositoryPlanConfigurer stubPersonDirectoryAttributeRepositoryPlanConfigurer(
            @Qualifier("stubAttributeRepositories")
            final ObjectProvider<List<IPersonAttributeDao>> stubAttributeRepositories) {
            return plan -> plan.registerAttributeRepositories(stubAttributeRepositories.getObject());
        }
    }
}
