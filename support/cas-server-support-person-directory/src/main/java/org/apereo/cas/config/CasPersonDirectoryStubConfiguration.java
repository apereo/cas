package org.apereo.cas.config;

import org.apereo.cas.authentication.attribute.StubPersonAttributeDao;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.model.core.authentication.AttributeRepositoryStates;
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesProperties;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlanConfigurer;
import org.apereo.cas.util.spring.beans.BeanContainer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is {@link CasPersonDirectoryStubConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Configuration(value = "CasPersonDirectoryStubConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.PersonDirectory, module = "stub")
class CasPersonDirectoryStubConfiguration {

    @Configuration(value = "StubAttributeRepositoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class StubAttributeRepositoryConfiguration {
        @ConditionalOnMissingBean(name = "stubAttributeRepositories")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public BeanContainer<PersonAttributeDao> stubAttributeRepositories(final CasConfigurationProperties casProperties) {
            val list = new ArrayList<PersonAttributeDao>();
            val stub = casProperties.getAuthn().getAttributeRepository().getStub();
            val attrs = stub.getAttributes();
            if (!attrs.isEmpty()) {
                val dao = newStubAttributeRepository(casProperties.getAuthn().getAttributeRepository());
                list.add(dao);
            }
            return BeanContainer.of(list);
        }

        private static PersonAttributeDao newStubAttributeRepository(final PrincipalAttributesProperties properties) {
            val dao = new StubPersonAttributeDao();
            val backingMap = new LinkedHashMap<String, List<Object>>();
            val stub = properties.getStub();
            stub.getAttributes().forEach((key, value) -> {
                val vals = StringUtils.commaDelimitedListToStringArray(value);
                backingMap.put(key, Arrays.stream(vals)
                    .map(v -> {
                        val result = BooleanUtils.toBooleanObject(v);
                        if (result != null) {
                            return result;
                        }
                        return v;
                    })
                    .collect(Collectors.toList()));
            });
            dao.setBackingMap(backingMap);
            dao.setOrder(stub.getOrder());
            dao.setEnabled(stub.getState() != AttributeRepositoryStates.DISABLED);
            dao.putTag("state", stub.getState() == AttributeRepositoryStates.ACTIVE);
            if (StringUtils.hasText(stub.getId())) {
                dao.setId(stub.getId());
            }
            return dao;
        }
    }

    @Configuration(value = "StubAttributeRepositoryPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class StubAttributeRepositoryPlanConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "stubPersonDirectoryAttributeRepositoryPlanConfigurer")
        public PersonDirectoryAttributeRepositoryPlanConfigurer stubPersonDirectoryAttributeRepositoryPlanConfigurer(
            @Qualifier("stubAttributeRepositories")
            final BeanContainer<PersonAttributeDao> stubAttributeRepositories) {
            return plan -> {
                val results = stubAttributeRepositories.toList()
                    .stream()
                    .filter(PersonAttributeDao::isEnabled)
                    .collect(Collectors.toList());
                plan.registerAttributeRepositories(results);
            };
        }
    }
}
