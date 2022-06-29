package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlanConfigurer;
import org.apereo.cas.syncope.SyncopePersonAttributeDao;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.BeanContainer;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

import java.util.List;

/**
 * This is {@link SyncopePersonDirectoryConfiguration}.
 *
 * @author Francesco Chicchiriccò
 * @since 6.5.0
 */
@Configuration(value = "SyncopePersonDirectoryConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SyncopePersonDirectoryConfiguration {

    @ConditionalOnMissingBean(name = "syncopePersonAttributeDaos")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public BeanContainer<IPersonAttributeDao> syncopePersonAttributeDaos(
        final CasConfigurationProperties casProperties) {

        val properties = casProperties.getAuthn().getAttributeRepository().getSyncope();
        if (StringUtils.isNotBlank(properties.getUrl()) && StringUtils.isNotBlank(properties.getSearchFilter())) {
            val dao = new SyncopePersonAttributeDao(properties);
            dao.setOrder(properties.getOrder());
            FunctionUtils.doIfNotNull(properties.getId(), dao::setId);
            return BeanContainer.of(CollectionUtils.wrapList(dao));
        }
        return BeanContainer.of(List.of());
    }

    @ConditionalOnMissingBean(name = "syncopeAttributeRepositoryPlanConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PersonDirectoryAttributeRepositoryPlanConfigurer syncopeAttributeRepositoryPlanConfigurer(
        @Qualifier("syncopePersonAttributeDaos")
        final BeanContainer<IPersonAttributeDao> syncopePersonAttributeDaos) {
        return plan -> syncopePersonAttributeDaos.toList().forEach(plan::registerAttributeRepository);
    }
}
