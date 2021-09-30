package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.okta.OktaPersonAttributeDao;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlanConfigurer;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;

import com.okta.sdk.client.Client;
import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.support.SimpleUsernameAttributeProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

import java.util.List;

/**
 * This is {@link OktaPersonDirectoryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Configuration(value = "oktaPersonDirectoryConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty("cas.authn.attribute-repository.okta.organization-url")
public class OktaPersonDirectoryConfiguration {
    @ConditionalOnMissingBean(name = "oktaPersonDirectoryClient")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public Client oktaPersonDirectoryClient(final CasConfigurationProperties casProperties) {
        val properties = casProperties.getAuthn().getAttributeRepository().getOkta();
        return OktaConfigurationFactory.buildClient(properties);
    }

    @ConditionalOnMissingBean(name = "oktaPersonAttributeDaos")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public List<IPersonAttributeDao> oktaPersonAttributeDaos(
        @Qualifier("oktaPersonDirectoryClient")
        final Client oktaPersonDirectoryClient,
        final CasConfigurationProperties casProperties) {
        val properties = casProperties.getAuthn().getAttributeRepository().getOkta();
        val dao = new OktaPersonAttributeDao(oktaPersonDirectoryClient);
        dao.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider(properties.getUsernameAttribute()));
        dao.setOrder(properties.getOrder());
        FunctionUtils.doIfNotNull(properties.getId(), dao::setId);
        return CollectionUtils.wrapList(dao);
    }

    @ConditionalOnMissingBean(name = "oktaAttributeRepositoryPlanConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public PersonDirectoryAttributeRepositoryPlanConfigurer oktaAttributeRepositoryPlanConfigurer(
        @Qualifier("oktaPersonAttributeDaos")
        final List<IPersonAttributeDao> oktaPersonAttributeDaos) {
        return plan -> oktaPersonAttributeDaos.forEach(plan::registerAttributeRepository);
    }

}
