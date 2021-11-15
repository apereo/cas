package org.apereo.cas.config;

import org.apereo.cas.authentication.principal.resolvers.InternalGroovyScriptDao;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.authentication.AttributeRepositoryStates;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlanConfigurer;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.BeanContainer;
import org.apereo.cas.util.spring.boot.ConditionalOnMultiValuedProperty;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.support.GroovyPersonAttributeDao;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * This is {@link CasPersonDirectoryGroovyConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@ConditionalOnMultiValuedProperty(name = "cas.authn.attribute-repository.groovy[0]", value = "location")
@Configuration(value = "CasPersonDirectoryGroovyConfiguration", proxyBeanMethods = false)
@Slf4j
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasPersonDirectoryGroovyConfiguration {

    @Configuration(value = "GroovyAttributeRepositoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class GroovyAttributeRepositoryConfiguration {
        @ConditionalOnMissingBean(name = "groovyAttributeRepositories")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public BeanContainer<IPersonAttributeDao> groovyAttributeRepositories(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            val list = new ArrayList<IPersonAttributeDao>();
            casProperties.getAuthn().getAttributeRepository().getGroovy()
                .stream()
                .filter(groovy -> groovy.getLocation() != null)
                .forEach(groovy -> {
                    val dao = new GroovyPersonAttributeDao(new InternalGroovyScriptDao(applicationContext, casProperties));
                    dao.setCaseInsensitiveUsername(groovy.isCaseInsensitive());
                    dao.setOrder(groovy.getOrder());
                    dao.setEnabled(groovy.getState() != AttributeRepositoryStates.DISABLED);
                    dao.putTag(PersonDirectoryAttributeRepositoryPlanConfigurer.class.getSimpleName(),
                        groovy.getState() == AttributeRepositoryStates.ACTIVE);
                    FunctionUtils.doIfNotNull(groovy.getId(), dao::setId);
                    LOGGER.debug("Configured Groovy attribute sources from [{}]", groovy.getLocation());
                    list.add(dao);
                });
            return BeanContainer.of(list);
        }
    }

    @Configuration(value = "GroovyAttributeRepositoryPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class GroovyAttributeRepositoryPlanConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "groovyPersonDirectoryAttributeRepositoryPlanConfigurer")
        public PersonDirectoryAttributeRepositoryPlanConfigurer groovyPersonDirectoryAttributeRepositoryPlanConfigurer(
            @Qualifier("groovyAttributeRepositories")
            final BeanContainer<IPersonAttributeDao> groovyAttributeRepositories) {
            return plan -> {
                val results = groovyAttributeRepositories.toList()
                    .stream()
                    .filter(repo -> (Boolean) repo.getTags().get(PersonDirectoryAttributeRepositoryPlanConfigurer.class.getSimpleName()))
                    .collect(Collectors.toList());
                plan.registerAttributeRepositories(results);
            };
        }
    }

}
