package org.apereo.cas.config;

import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.attribute.DefaultAttributeDefinitionStore;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolutionExecutionPlanConfigurer;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ResourceUtils;

import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.support.StubPersonAttributeDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

import java.util.List;
import java.util.Map;

/**
 * This is {@link CasPersonDirectoryTestConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@TestConfiguration("casPersonDirectoryTestConfiguration")
@Lazy(false)
@ConditionalOnProperty(value = "spring.boot.config.CasPersonDirectoryTestConfiguration.enabled",
                       havingValue = "true", matchIfMissing = true)
public class CasPersonDirectoryTestConfiguration {
    @Bean
    public List<IPersonAttributeDao> attributeRepositories() {
        return CollectionUtils.wrap(attributeRepository());
    }

    @ConditionalOnMissingBean(name = PrincipalResolver.BEAN_NAME_ATTRIBUTE_REPOSITORY)
    @Bean
    public IPersonAttributeDao attributeRepository() {
        val attrs = CollectionUtils.wrap(
            "uid", CollectionUtils.wrap("uid"),
            "eduPersonAffiliation", CollectionUtils.wrap("developer"),
            "groupMembership", CollectionUtils.wrap("adopters"));
        return new StubPersonAttributeDao((Map) attrs);
    }

    @ConditionalOnMissingBean(name = AttributeDefinitionStore.BEAN_NAME)
    @Bean
    @Autowired
    public AttributeDefinitionStore attributeDefinitionStore(final CasConfigurationProperties casProperties) throws Exception {
        val resource = casProperties.getAuthn().getAttributeRepository()
            .getAttributeDefinitionStore().getJson().getLocation();
        if (ResourceUtils.doesResourceExist(resource)) {
            return new DefaultAttributeDefinitionStore(resource);
        }
        return new DefaultAttributeDefinitionStore();
    }

    @Bean
    @Autowired
    public PrincipalResolutionExecutionPlanConfigurer testPersonDirectoryPrincipalResolutionExecutionPlanConfigurer(
        final CasConfigurationProperties casProperties) {
        return plan -> {
            val personDirectory = casProperties.getPersonDirectory();
            val resolver = CoreAuthenticationUtils.newPersonDirectoryPrincipalResolver(PrincipalFactoryUtils.newPrincipalFactory(),
                attributeRepository(),
                CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger()),
                personDirectory);
            plan.registerPrincipalResolver(resolver);
        };
    }
}
