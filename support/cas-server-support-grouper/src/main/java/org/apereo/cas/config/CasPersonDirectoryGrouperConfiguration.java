package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.authentication.AttributeRepositoryStates;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlanConfigurer;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.BeanContainer;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.support.GrouperPersonAttributeDao;
import org.apereo.services.persondir.support.SimpleUsernameAttributeProvider;
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
@Configuration(value = "CasPersonDirectoryGrouperConfiguration", proxyBeanMethods = false)
@Slf4j
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasPersonDirectoryGrouperConfiguration {
    @Configuration(value = "GrouperAttributeRepositoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class GrouperAttributeRepositoryConfiguration {
        @ConditionalOnMissingBean(name = "grouperAttributeRepositories")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public BeanContainer<IPersonAttributeDao> grouperAttributeRepositories(final CasConfigurationProperties casProperties) {
            val list = new ArrayList<IPersonAttributeDao>();
            val gp = casProperties.getAuthn().getAttributeRepository().getGrouper();
            val dao = new GrouperPersonAttributeDao();
            dao.setOrder(gp.getOrder());
            dao.setParameters(gp.getParameters());
            dao.setSubjectType(GrouperPersonAttributeDao.GrouperSubjectType.valueOf(gp.getSubjectType()));
            dao.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider(gp.getUsernameAttribute()));
            dao.setEnabled(gp.getState() != AttributeRepositoryStates.DISABLED);
            dao.putTag(PersonDirectoryAttributeRepositoryPlanConfigurer.class.getSimpleName(),
                gp.getState() == AttributeRepositoryStates.ACTIVE);
            FunctionUtils.doIfNotNull(gp.getId(), dao::setId);
            LOGGER.debug("Configured Grouper attribute source");
            list.add(dao);
            return BeanContainer.of(list);
        }
    }

    @Configuration(value = "GrouperAttributeRepositoryPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class GrouperAttributeRepositoryPlanConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "grouperPersonDirectoryAttributeRepositoryPlanConfigurer")
        public PersonDirectoryAttributeRepositoryPlanConfigurer grouperPersonDirectoryAttributeRepositoryPlanConfigurer(
            @Qualifier("grouperAttributeRepositories")
            final BeanContainer<IPersonAttributeDao> grouperAttributeRepositories) {
            return plan -> {
                val results = grouperAttributeRepositories.toList()
                    .stream()
                    .filter(repo -> (Boolean) repo.getTags().get(PersonDirectoryAttributeRepositoryPlanConfigurer.class.getSimpleName()))
                    .collect(Collectors.toList());
                plan.registerAttributeRepositories(results);
            };
        }
    }
}
