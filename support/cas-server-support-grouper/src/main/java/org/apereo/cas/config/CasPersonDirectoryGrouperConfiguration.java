package org.apereo.cas.config;

import org.apereo.cas.authentication.attribute.SimpleUsernameAttributeProvider;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.model.core.authentication.AttributeRepositoryStates;
import org.apereo.cas.persondir.GrouperPersonAttributeDao;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlanConfigurer;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.beans.BeanContainer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * This is {@link CasPersonDirectoryGrouperConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Slf4j
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.PersonDirectory, module = "grouper")
@Configuration(value = "CasPersonDirectoryGrouperConfiguration", proxyBeanMethods = false)
class CasPersonDirectoryGrouperConfiguration {
    @Configuration(value = "GrouperAttributeRepositoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class GrouperAttributeRepositoryConfiguration {
        @ConditionalOnMissingBean(name = "grouperAttributeRepositories")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public BeanContainer<PersonAttributeDao> grouperAttributeRepositories(
            final CasConfigurationProperties casProperties) {
            val list = new ArrayList<PersonAttributeDao>();
            val gp = casProperties.getAuthn().getAttributeRepository().getGrouper();
            val dao = new GrouperPersonAttributeDao();
            dao.setOrder(gp.getOrder());
            dao.setParameters(gp.getParameters());
            dao.setSubjectType(GrouperPersonAttributeDao.GrouperSubjectType.valueOf(gp.getSubjectType()));
            dao.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider(gp.getUsernameAttribute()));
            dao.setEnabled(gp.getState() != AttributeRepositoryStates.DISABLED);
            dao.putTag("state", gp.getState());
            FunctionUtils.doIfNotNull(gp.getId(), id -> dao.setId(id));
            LOGGER.debug("Configured Grouper attribute source");
            list.add(dao);
            return BeanContainer.of(list);
        }
    }

    @Configuration(value = "GrouperAttributeRepositoryPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class GrouperAttributeRepositoryPlanConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "grouperPersonDirectoryAttributeRepositoryPlanConfigurer")
        public PersonDirectoryAttributeRepositoryPlanConfigurer grouperPersonDirectoryAttributeRepositoryPlanConfigurer(
            @Qualifier("grouperAttributeRepositories")
            final BeanContainer<PersonAttributeDao> grouperAttributeRepositories) {
            return plan -> {
                val results = grouperAttributeRepositories.toList()
                    .stream()
                    .filter(PersonAttributeDao::isEnabled)
                    .collect(Collectors.toList());
                plan.registerAttributeRepositories(results);
            };
        }
    }
}
