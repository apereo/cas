package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlanConfigurer;
import org.apereo.cas.syncope.SyncopePersonAttributeDao;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanContainer;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link SyncopePersonDirectoryConfiguration}.
 *
 * @author Francesco Chicchiriccò
 * @since 6.5.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.PersonDirectory, module = "syncope")
@AutoConfiguration
public class SyncopePersonDirectoryConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.attribute-repository.syncope.url").isUrl();

    @ConditionalOnMissingBean(name = "syncopePersonAttributeDaos")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @SuppressWarnings("unchecked")
    public BeanContainer<IPersonAttributeDao> syncopePersonAttributeDaos(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) throws Exception {

        return BeanSupplier.of(BeanContainer.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val properties = casProperties.getAuthn().getAttributeRepository().getSyncope();
                val dao = new SyncopePersonAttributeDao(properties);
                dao.setOrder(properties.getOrder());
                FunctionUtils.doIfNotNull(properties.getId(), dao::setId);
                return BeanContainer.of(CollectionUtils.wrapList(dao));
            })
            .otherwise(BeanContainer::empty)
            .get();
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
