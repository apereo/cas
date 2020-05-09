package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.LdapPasswordSynchronizationAuthenticationPostProcessor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.authentication.passwordsync.LdapPasswordSynchronizationProperties;

import lombok.SneakyThrows;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ListFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link LdapPasswordSynchronizationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration(value = "ldapPasswordSynchronizationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class LdapPasswordSynchronizationConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    @SneakyThrows
    public ListFactoryBean ldapPasswordSynchronizationAuthenticationPostProcessorListFactoryBean() {
        val bean = new ListFactoryBean() {
            @Override
            protected void destroyInstance(final List list) {
                list.forEach(Unchecked.consumer(postProcessor -> {
                    ((DisposableBean) postProcessor).destroy();
                }));
            }
        };
        bean.setSourceList(new ArrayList());
        return bean;
    }

    @ConditionalOnMissingBean(name = "ldapPasswordSynchronizationAuthenticationEventExecutionPlanConfigurer")
    @Bean
    @SneakyThrows
    @Autowired
    @Qualifier("ldapPasswordSynchronizationAuthenticationPostProcessorListFactoryBean")
    public AuthenticationEventExecutionPlanConfigurer ldapPasswordSynchronizationAuthenticationEventExecutionPlanConfigurer(
            final ListFactoryBean ldapPasswordSynchronizationAuthenticationPostProcessorListFactoryBean) {
        val postProcessorList = ldapPasswordSynchronizationAuthenticationPostProcessorListFactoryBean.getObject();
        return plan -> {
            val ldap = casProperties.getAuthn().getPasswordSync().getLdap();
            ldap.stream()
                .filter(LdapPasswordSynchronizationProperties::isEnabled)
                .forEach(instance -> {
                    val postProcessor = new LdapPasswordSynchronizationAuthenticationPostProcessor(instance);
                    postProcessorList.add(postProcessor);
                    plan.registerAuthenticationPostProcessor(postProcessor);
                });
        };
    }
}
