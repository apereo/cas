package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlanConfigurer;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.boot.ConditionalOnMultiValuedProperty;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.support.RestfulPersonAttributeDao;
import org.apereo.services.persondir.support.SimpleUsernameAttributeProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This is {@link CasPersonDirectoryRestConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */

@ConditionalOnMultiValuedProperty(name = "cas.authn.attribute-repository.rest[0]", value = "url")
@Configuration(value = "CasPersonDirectoryRestConfiguration", proxyBeanMethods = false)
@Slf4j
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasPersonDirectoryRestConfiguration {
    @ConditionalOnMissingBean(name = "restfulAttributeRepositories")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public List<IPersonAttributeDao> restfulAttributeRepositories(final CasConfigurationProperties casProperties) {
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
                dao.setMethod(Objects.requireNonNull(HttpMethod.resolve(rest.getMethod())).name());

                val headers = CollectionUtils.<String, String>wrap("Content-Type", MediaType.APPLICATION_JSON_VALUE);
                headers.putAll(rest.getHeaders());
                dao.setHeaders(headers);
                dao.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider(rest.getUsernameAttribute()));

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

        return list;
    }

    @Bean
    @Autowired
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PersonDirectoryAttributeRepositoryPlanConfigurer restfulPersonDirectoryAttributeRepositoryPlanConfigurer(
        @Qualifier("restfulAttributeRepositories") final ObjectProvider<List<IPersonAttributeDao>> restfulAttributeRepositories) {
        return plan -> plan.registerAttributeRepositories(restfulAttributeRepositories.getObject());
    }
}


