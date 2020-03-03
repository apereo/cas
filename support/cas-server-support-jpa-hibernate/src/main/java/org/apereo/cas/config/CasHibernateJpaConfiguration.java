package org.apereo.cas.config;

/**
 * This is {@link CasHibernateJpaConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.hibernate.CasHibernateJpaBeanFactory;
import org.apereo.cas.jpa.JpaBeanFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(value = "casHibernateJpaConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasHibernateJpaConfiguration {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @RefreshScope
    @Bean
    public JpaBeanFactory jpaBeanFactory() {
        return new CasHibernateJpaBeanFactory(applicationContext);
    }
}
