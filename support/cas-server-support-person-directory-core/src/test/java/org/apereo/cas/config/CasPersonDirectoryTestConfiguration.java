package org.apereo.cas.config;

import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.attribute.AttributeRepositoryResolver;
import org.apereo.cas.authentication.attribute.DefaultAttributeDefinitionStore;
import org.apereo.cas.authentication.attribute.StubPersonAttributeDao;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolutionExecutionPlanConfigurer;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.principal.resolvers.PersonDirectoryPrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import java.util.List;
import java.util.Map;

/**
 * This is {@link CasPersonDirectoryTestConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@TestConfiguration(value = "casPersonDirectoryTestConfiguration", proxyBeanMethods = false)
@ConditionalOnProperty(value = "spring.boot.config.CasPersonDirectoryTestConfiguration.enabled",
    havingValue = "true", matchIfMissing = true)
public class CasPersonDirectoryTestConfiguration {
    @Bean
    public List<PersonAttributeDao> attributeRepositories(
        @Qualifier(PrincipalResolver.BEAN_NAME_ATTRIBUTE_REPOSITORY) final PersonAttributeDao attributeRepository) {
        return CollectionUtils.wrap(attributeRepository);
    }

    @ConditionalOnMissingBean(name = PrincipalResolver.BEAN_NAME_ATTRIBUTE_REPOSITORY)
    @Bean
    public PersonAttributeDao attributeRepository() {
        val attrs = CollectionUtils.wrap(
            "uid", CollectionUtils.wrap("uid"),
            "mail", CollectionUtils.wrap("cas@apereo.org"),
            "eduPersonAffiliation", CollectionUtils.wrap("developer"),
            "groupMembership", CollectionUtils.wrap("adopters"));
        return new StubPersonAttributeDao((Map) attrs);
    }

    @ConditionalOnMissingBean(name = AttributeDefinitionStore.BEAN_NAME)
    @Bean
    public AttributeDefinitionStore attributeDefinitionStore(
        final CasConfigurationProperties casProperties) throws Exception {
        val resource = casProperties.getAuthn().getAttributeRepository()
            .getAttributeDefinitionStore().getJson().getLocation();
        val store = new DefaultAttributeDefinitionStore(resource);
        store.setScope(casProperties.getServer().getScope());
        return store;
    }

    @Bean
    public PrincipalResolutionExecutionPlanConfigurer testPersonDirectoryPrincipalResolutionExecutionPlanConfigurer(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(AttributeDefinitionStore.BEAN_NAME)
        final AttributeDefinitionStore attributeDefinitionStore,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        final CasConfigurationProperties casProperties,
        @Qualifier(PrincipalResolver.BEAN_NAME_ATTRIBUTE_REPOSITORY)
        final PersonAttributeDao attributeRepository,
        @Qualifier(AttributeRepositoryResolver.BEAN_NAME)
        final AttributeRepositoryResolver attributeRepositoryResolver) {
        return plan -> {
            val personDirectory = casProperties.getPersonDirectory();
            val attributeMerger = CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger());
            val resolver = PersonDirectoryPrincipalResolver.newPersonDirectoryPrincipalResolver(
                applicationContext,
                PrincipalFactoryUtils.newPrincipalFactory(),
                attributeRepository, attributeMerger,
                servicesManager, attributeDefinitionStore,
                attributeRepositoryResolver, personDirectory);
            plan.registerPrincipalResolver(resolver);
        };
    }
}
