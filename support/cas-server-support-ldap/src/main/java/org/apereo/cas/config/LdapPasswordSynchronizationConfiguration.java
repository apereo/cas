package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.LdapPasswordSynchronizationAuthenticationPostProcessor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.authentication.passwordsync.LdapPasswordSynchronizationProperties;

import lombok.SneakyThrows;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ListFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This is {@link LdapPasswordSynchronizationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration(value = "ldapPasswordSynchronizationConfiguration", proxyBeanMethods = true)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cas.authn.password-sync", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LdapPasswordSynchronizationConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;


    @Bean
    @SneakyThrows
    public ListFactoryBean ldapPasswordSynchronizationAuthenticationPostProcessorListFactoryBean() {
        val bean = new ListFactoryBean() {
            @Override
            protected void destroyInstance(final List list) {
                Objects.requireNonNull(list).forEach(Unchecked.consumer(postProcessor ->
                    ((DisposableBean) postProcessor).destroy()
                ));
            }
        };
        bean.setSourceList(new ArrayList<>());
        return bean;
    }

    @ConditionalOnMissingBean(name = "ldapPasswordSynchronizationAuthenticationEventExecutionPlanConfigurer")
    @Bean
    @Autowired
    public AuthenticationEventExecutionPlanConfigurer ldapPasswordSynchronizationAuthenticationEventExecutionPlanConfigurer(
        @Qualifier("ldapPasswordSynchronizationAuthenticationPostProcessorListFactoryBean")
        final ListFactoryBean ldapPasswordSynchronizationAuthenticationPostProcessorListFactoryBean) {
        return plan -> {
            try {
                val postProcessorList = Objects.requireNonNull(ldapPasswordSynchronizationAuthenticationPostProcessorListFactoryBean.getObject());
                val ldap = casProperties.getAuthn().getPasswordSync().getLdap();
                ldap.stream()
                    .filter(LdapPasswordSynchronizationProperties::isEnabled)
                    .forEach(instance -> {
                        val postProcessor = new LdapPasswordSynchronizationAuthenticationPostProcessor(instance);
                        postProcessorList.add(postProcessor);
                        plan.registerAuthenticationPostProcessor(postProcessor);
                    });
            } catch (final Exception e) {
                throw new BeanCreationException("Error creating ldapPasswordSynchronizationAuthenticationEventExecutionPlanConfigurer: " + e.getMessage(), e);
            }
        };
    }
}
