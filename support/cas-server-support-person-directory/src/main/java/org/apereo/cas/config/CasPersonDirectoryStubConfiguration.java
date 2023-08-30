package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlanConfigurer;
import org.apereo.cas.util.spring.beans.BeanContainer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
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
 * This is {@link CasPersonDirectoryStubConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Configuration(value = "CasPersonDirectoryStubConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.PersonDirectory)
public class CasPersonDirectoryStubConfiguration {

    @Configuration(value = "StubAttributeRepositoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class StubAttributeRepositoryConfiguration {
        @ConditionalOnMissingBean(name = "stubAttributeRepositories")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public BeanContainer<IPersonAttributeDao> stubAttributeRepositories(final CasConfigurationProperties casProperties) {
            val list = new ArrayList<IPersonAttributeDao>();
            val stub = casProperties.getAuthn().getAttributeRepository().getStub();
            val attrs = stub.getAttributes();
            if (!attrs.isEmpty()) {
                val dao = Beans.newStubAttributeRepository(casProperties.getAuthn().getAttributeRepository());
                list.add(dao);
            }
            return BeanContainer.of(list);
        }
    }

    @Configuration(value = "StubAttributeRepositoryPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class StubAttributeRepositoryPlanConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "stubPersonDirectoryAttributeRepositoryPlanConfigurer")
        public PersonDirectoryAttributeRepositoryPlanConfigurer stubPersonDirectoryAttributeRepositoryPlanConfigurer(
            @Qualifier("stubAttributeRepositories")
            final BeanContainer<IPersonAttributeDao> stubAttributeRepositories) {
            return plan -> {
                val results = stubAttributeRepositories.toList()
                    .stream()
                    .filter(IPersonAttributeDao::isEnabled)
                    .collect(Collectors.toList());
                plan.registerAttributeRepositories(results);
            };
        }
    }
}
