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
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * This is {@link OktaPersonDirectoryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Configuration(value = "oktaPersonDirectoryConfiguration", proxyBeanMethods = true)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty("cas.authn.attribute-repository.okta.organization-url")
public class OktaPersonDirectoryConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "oktaPersonDirectoryClient")
    @Bean
    @RefreshScope
    public Client oktaPersonDirectoryClient() {
        val properties = casProperties.getAuthn().getAttributeRepository().getOkta();
        return OktaConfigurationFactory.buildClient(properties);
    }

    @ConditionalOnMissingBean(name = "oktaPersonAttributeDaos")
    @Bean
    @RefreshScope
    public List<IPersonAttributeDao> oktaPersonAttributeDaos() {
        val properties = casProperties.getAuthn().getAttributeRepository().getOkta();
        val dao = new OktaPersonAttributeDao(oktaPersonDirectoryClient());
        dao.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider(properties.getUsernameAttribute()));
        dao.setOrder(properties.getOrder());
        FunctionUtils.doIfNotNull(properties.getId(), dao::setId);
        return CollectionUtils.wrapList(dao);
    }

    @ConditionalOnMissingBean(name = "oktaAttributeRepositoryPlanConfigurer")
    @Bean
    @RefreshScope
    public PersonDirectoryAttributeRepositoryPlanConfigurer oktaAttributeRepositoryPlanConfigurer() {
        return plan -> {
            val daos = oktaPersonAttributeDaos();
            daos.forEach(plan::registerAttributeRepository);
        };
    }

}
