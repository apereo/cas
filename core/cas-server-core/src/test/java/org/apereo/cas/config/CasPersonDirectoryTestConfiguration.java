package org.apereo.cas.config;

import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.attribute.DefaultAttributeDefinitionStore;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolutionExecutionPlanConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ResourceUtils;

import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.support.StubPersonAttributeDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
public class CasPersonDirectoryTestConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;
    
    @Bean
    public List<IPersonAttributeDao> attributeRepositories() {
        return CollectionUtils.wrap(attributeRepository());
    }

    @ConditionalOnMissingBean(name = "attributeRepository")
    @Bean
    public IPersonAttributeDao attributeRepository() {
        val attrs = CollectionUtils.wrap("uid", CollectionUtils.wrap("uid"),
            "eduPersonAffiliation", CollectionUtils.wrap("developer"),
            "groupMembership", CollectionUtils.wrap("adopters"));
        return new StubPersonAttributeDao((Map) attrs);
    }

    @ConditionalOnMissingBean(name = "attributeDefinitionStore")
    @Bean
    public AttributeDefinitionStore attributeDefinitionStore() throws Exception {
        val resource = casProperties.getPersonDirectory()
            .getAttributeDefinitionStore().getJson().getLocation();
        if (ResourceUtils.doesResourceExist(resource)) {
            return new DefaultAttributeDefinitionStore(resource);
        }
        return new DefaultAttributeDefinitionStore();
    }
    
    @Bean
    public PrincipalResolutionExecutionPlanConfigurer testPersonDirectoryPrincipalResolutionExecutionPlanConfigurer() {
        return plan -> {
            val personDirectory = casProperties.getPersonDirectory();
            val resolver = CoreAuthenticationUtils.newPersonDirectoryPrincipalResolver(PrincipalFactoryUtils.newPrincipalFactory(),
                attributeRepository(), personDirectory);
            plan.registerPrincipalResolver(resolver);
        };
    }
}
