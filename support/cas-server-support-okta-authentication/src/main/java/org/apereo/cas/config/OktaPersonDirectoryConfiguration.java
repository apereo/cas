package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.okta.OktaPersonAttributeDao;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlanConfigurer;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.BeanContainer;

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
    public BeanContainer<IPersonAttributeDao> oktaPersonAttributeDaos(
        @Qualifier("oktaPersonDirectoryClient")
        final Client oktaPersonDirectoryClient,
        final CasConfigurationProperties casProperties) {
        val properties = casProperties.getAuthn().getAttributeRepository().getOkta();
        val dao = new OktaPersonAttributeDao(oktaPersonDirectoryClient);
        dao.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider(properties.getUsernameAttribute()));
        dao.setOrder(properties.getOrder());
        FunctionUtils.doIfNotNull(properties.getId(), dao::setId);
        return BeanContainer.of(CollectionUtils.wrapList(dao));
    }

    @ConditionalOnMissingBean(name = "oktaAttributeRepositoryPlanConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public PersonDirectoryAttributeRepositoryPlanConfigurer oktaAttributeRepositoryPlanConfigurer(
        @Qualifier("oktaPersonAttributeDaos")
        final BeanContainer<IPersonAttributeDao> oktaPersonAttributeDaos) {
        return plan -> oktaPersonAttributeDaos.toList().forEach(plan::registerAttributeRepository);
    }

}
