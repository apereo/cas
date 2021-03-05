package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlan;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlanConfigurer;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.support.GrouperPersonAttributeDao;
import org.apereo.services.persondir.support.SimpleUsernameAttributeProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link CasPersonDirectoryGrouperConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@ConditionalOnProperty(prefix = "cas.authn.attribute-repository.grouper", name = "enabled", havingValue = "true")
@Configuration("CasPersonDirectoryGrouperConfiguration")
@Slf4j
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasPersonDirectoryGrouperConfiguration implements PersonDirectoryAttributeRepositoryPlanConfigurer {
    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "grouperAttributeRepositories")
    @Bean
    @RefreshScope
    public List<IPersonAttributeDao> grouperAttributeRepositories() {
        val list = new ArrayList<IPersonAttributeDao>();
        val gp = casProperties.getAuthn().getAttributeRepository().getGrouper();

        if (gp.isEnabled()) {
            val dao = new GrouperPersonAttributeDao();
            dao.setOrder(gp.getOrder());
            dao.setParameters(gp.getParameters());
            dao.setSubjectType(GrouperPersonAttributeDao.GrouperSubjectType.valueOf(gp.getSubjectType()));
            dao.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider(gp.getUsernameAttribute()));
            FunctionUtils.doIfNotNull(gp.getId(), dao::setId);
            LOGGER.debug("Configured Grouper attribute source");
            list.add(dao);
        }
        return list;
    }

    @Override
    public void configureAttributeRepositoryPlan(final PersonDirectoryAttributeRepositoryPlan plan) {
        plan.registerAttributeRepositories(grouperAttributeRepositories());
    }
}
