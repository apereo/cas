package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.model.core.authentication.AttributeRepositoryStates;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlanConfigurer;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanContainer;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.support.RestfulPersonAttributeDao;
import org.apereo.services.persondir.support.SimpleUsernameAttributeProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This is {@link CasPersonDirectoryRestConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */

@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.PersonDirectory)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfiguration
public class CasPersonDirectoryRestConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.attribute-repository.rest[0].url").isUrl();

    @Configuration(value = "RestfulAttributeRepositoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class RestfulAttributeRepositoryConfiguration {
        @ConditionalOnMissingBean(name = "restfulAttributeRepositories")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public BeanContainer<IPersonAttributeDao> restfulAttributeRepositories(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(BeanContainer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val list = new ArrayList<IPersonAttributeDao>();
                    casProperties.getAuthn().getAttributeRepository().getRest()
                        .stream()
                        .filter(rest -> StringUtils.isNotBlank(rest.getUrl()))
                        .forEach(rest -> {
                            val dao = new RestfulPersonAttributeDao();
                            dao.setCaseInsensitiveUsername(rest.isCaseInsensitive());
                            dao.setOrder(rest.getOrder());
                            FunctionUtils.doIfNotNull(rest.getId(), dao::setId);
                            dao.setUrl(rest.getUrl());
                            dao.setMethod(Objects.requireNonNull(HttpMethod.valueOf(rest.getMethod())).name());
                            dao.setEnabled(rest.getState() != AttributeRepositoryStates.DISABLED);

                            val headers = CollectionUtils.<String, String>wrap("Content-Type", MediaType.APPLICATION_JSON_VALUE);
                            headers.putAll(rest.getHeaders());
                            dao.setHeaders(headers);
                            dao.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider(rest.getUsernameAttribute()));
                            dao.putTag(PersonDirectoryAttributeRepositoryPlanConfigurer.class.getSimpleName(),
                                rest.getState() == AttributeRepositoryStates.ACTIVE);

                            if (StringUtils.isNotBlank(rest.getBasicAuthPassword()) && StringUtils.isNotBlank(rest.getBasicAuthUsername())) {
                                dao.setBasicAuthPassword(rest.getBasicAuthPassword());
                                dao.setBasicAuthUsername(rest.getBasicAuthUsername());
                                LOGGER.debug("Basic authentication credentials are located for REST endpoint [{}]", rest.getUrl());
                            } else {
                                LOGGER.debug("Basic authentication credentials are not defined for REST endpoint [{}]", rest.getUrl());
                            }

                            LOGGER.debug("Configured REST attribute sources from [{}]", rest.getUrl());
                            list.add(dao);
                        });
                    return BeanContainer.of(list);
                })
                .otherwise(BeanContainer::empty)
                .get();
        }
    }

    @Configuration(value = "RestfulAttributeRepositoryPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class RestfulAttributeRepositoryPlanConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "restfulPersonDirectoryAttributeRepositoryPlanConfigurer")
        public PersonDirectoryAttributeRepositoryPlanConfigurer restfulPersonDirectoryAttributeRepositoryPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("restfulAttributeRepositories")
            final BeanContainer<IPersonAttributeDao> restfulAttributeRepositories) {
            return BeanSupplier.of(PersonDirectoryAttributeRepositoryPlanConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> plan -> {
                    val results = restfulAttributeRepositories.toList()
                        .stream()
                        .filter(repo -> (Boolean) repo.getTags().get(PersonDirectoryAttributeRepositoryPlanConfigurer.class.getSimpleName()))
                        .collect(Collectors.toList());
                    plan.registerAttributeRepositories(results);
                })
                .otherwiseProxy()
                .get();
        }
    }
}


