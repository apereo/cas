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

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.support.AbstractAggregatingDefaultQueryPersonAttributeDao;
import org.apereo.services.persondir.support.CachingPersonAttributeDaoImpl;
import org.apereo.services.persondir.support.CascadingPersonAttributeDao;
import org.apereo.services.persondir.support.MergingPersonAttributeDaoImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * This is {@link CasPersonDirectoryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casPersonDirectoryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasPersonDirectoryConfiguration {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "personDirectoryPrincipalFactory")
    @Bean
    @RefreshScope
    public PrincipalFactory personDirectoryPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "personDirectoryAttributeRepositoryPrincipalResolver")
    public PrincipalResolver personDirectoryAttributeRepositoryPrincipalResolver() {
        val personDirectory = casProperties.getPersonDirectory();
        return CoreAuthenticationUtils.newPersonDirectoryPrincipalResolver(personDirectoryPrincipalFactory(),
            attributeRepository(),
            CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger()),
            personDirectory);
    }

    @ConditionalOnMissingBean(name = "principalResolutionExecutionPlanConfigurer")
    @Bean
    @RefreshScope
    public PrincipalResolutionExecutionPlanConfigurer principalResolutionExecutionPlanConfigurer() {
        return plan -> {
            val personDirectoryPlan = personDirectoryAttributeRepositoryPlan();
            if (personDirectoryPlan.isEmpty()) {
                LOGGER.debug("Attribute repository sources are not available for person-directory principal resolution");
            } else {
                LOGGER.trace("Attribute repository sources are defined and available for person-directory principal resolution chain. ");
                plan.registerPrincipalResolver(personDirectoryAttributeRepositoryPrincipalResolver());
            }
        };
    }

    @ConditionalOnMissingBean(name = AttributeDefinitionStore.BEAN_NAME)
    @Bean
    @RefreshScope
    public AttributeDefinitionStore attributeDefinitionStore() throws Exception {
        val resource = casProperties.getAuthn().getAttributeRepository().getAttributeDefinitionStore().getJson().getLocation();
        val store = new DefaultAttributeDefinitionStore(resource);
        store.setScope(casProperties.getServer().getScope());
        return store;
    }

    @ConditionalOnMissingBean(name = "personDirectoryAttributeRepositoryPlan")
    @Bean
    @RefreshScope
    public PersonDirectoryAttributeRepositoryPlan personDirectoryAttributeRepositoryPlan() {
        val configurers = applicationContext
            .getBeansOfType(PersonDirectoryAttributeRepositoryPlanConfigurer.class, false, true)
            .values();

        val customizers = new ArrayList<>(applicationContext
            .getBeansOfType(PersonDirectoryAttributeRepositoryCustomizer.class, false, true)
            .values());

        val plan = new DefaultPersonDirectoryAttributeRepositoryPlan(customizers);
        configurers.forEach(c -> c.configureAttributeRepositoryPlan(plan));
        plan.registerAttributeRepositories(stubAttributeRepositories());
        AnnotationAwareOrderComparator.sort(plan.getAttributeRepositories());
        LOGGER.trace("Final list of attribute repositories is [{}]", plan.getAttributeRepositories());
        return plan;
    }

    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @ConditionalOnMissingBean(name = PrincipalResolver.BEAN_NAME_ATTRIBUTE_REPOSITORY)
    @Bean
    @RefreshScope
    public IPersonAttributeDao attributeRepository() {
        return cachingAttributeRepository();
    }

    @ConditionalOnMissingBean(name = "stubAttributeRepositories")
    @Bean
    @RefreshScope
    public List<IPersonAttributeDao> stubAttributeRepositories() {
        val list = new ArrayList<IPersonAttributeDao>();
        val stub = casProperties.getAuthn().getAttributeRepository().getStub();
        val attrs = stub.getAttributes();
        if (!attrs.isEmpty()) {
            LOGGER.info("Found and added static attributes [{}] to the list of candidate attribute repositories", attrs.keySet());
            val dao = Beans.newStubAttributeRepository(casProperties.getAuthn().getAttributeRepository());
            list.add(dao);
        }
        return list;
    }

    @Bean
    @ConditionalOnMissingBean(name = "cachingAttributeRepository")
    @RefreshScope
    public IPersonAttributeDao cachingAttributeRepository() {
        val props = casProperties.getAuthn().getAttributeRepository().getCore();
        if (props.getExpirationTime() <= 0) {
            LOGGER.warn("Attribute repository caching is disabled");
            return aggregatingAttributeRepository();
        }

        val impl = new CachingPersonAttributeDaoImpl();
        impl.setCacheNullResults(false);
        val userinfoCache = Caffeine.newBuilder()
            .maximumSize(props.getMaximumCacheSize())
            .expireAfterWrite(props.getExpirationTime(), TimeUnit.valueOf(props.getExpirationTimeUnit().toUpperCase()))
            .build();
        impl.setUserInfoCache((Map) userinfoCache.asMap());
        impl.setCachedPersonAttributesDao(aggregatingAttributeRepository());
        LOGGER.trace("Configured cache expiration policy for attribute sources to be [{}] minute(s)", props.getExpirationTime());
        return impl;
    }

    @Bean
    @ConditionalOnMissingBean(name = "aggregatingAttributeRepository")
    @RefreshScope
    public IPersonAttributeDao aggregatingAttributeRepository() {
        val aggregate = getAggregateAttributeRepository();

        val properties = casProperties.getAuthn().getAttributeRepository();
        val attributeMerger = CoreAuthenticationUtils.getAttributeMerger(properties.getCore().getMerger());
        LOGGER.trace("Configured merging strategy for attribute sources is [{}]", attributeMerger);
        aggregate.setMerger(attributeMerger);

        val list = personDirectoryAttributeRepositoryPlan().getAttributeRepositories();
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

    private AbstractAggregatingDefaultQueryPersonAttributeDao getAggregateAttributeRepository() {
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

}
