package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigurationContext;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.CasEventRepositoryFilter;
import org.apereo.cas.support.events.jpa.JpaCasEvent;
import org.apereo.cas.support.events.jpa.JpaCasEventRepository;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import java.util.List;

/**
 * This is {@link JpaEventsConfiguration}, defines certain beans via configuration
 * while delegating some to Spring namespaces inside the context config file.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("jpaEventsConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = true)
public class JpaEventsConfiguration {
    @Autowired
    @Qualifier("jpaBeanFactory")
    private ObjectProvider<JpaBeanFactory> jpaBeanFactory;

    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @Bean
    public JpaVendorAdapter jpaEventVendorAdapter() {
        return jpaBeanFactory.getObject().newJpaVendorAdapter(casProperties.getJdbc());
    }

    @Bean
    @ConditionalOnMissingBean(name = "dataSourceEvent")
    @RefreshScope
    public DataSource dataSourceEvent() {
        return JpaBeans.newDataSource(casProperties.getEvents().getJpa());
    }

    @Bean
    public List<String> jpaEventPackagesToScan() {
        return CollectionUtils.wrap(JpaCasEvent.class.getPackage().getName());
    }

    @Lazy
    @Bean
    public LocalContainerEntityManagerFactoryBean eventsEntityManagerFactory() {

        val factory = jpaBeanFactory.getObject();
        val ctx = new JpaConfigurationContext(
            jpaEventVendorAdapter(),
            "jpaEventRegistryContext",
            jpaEventPackagesToScan(),
            dataSourceEvent());
        return factory.newEntityManagerFactoryBean(ctx, casProperties.getEvents().getJpa());
    }

    @Autowired
    @Bean
    public PlatformTransactionManager transactionManagerEvents(@Qualifier("eventsEntityManagerFactory") final EntityManagerFactory emf) {
        val mgmr = new JpaTransactionManager();
        mgmr.setEntityManagerFactory(emf);
        return mgmr;
    }

    @ConditionalOnMissingBean(name = "jpaEventRepositoryFilter")
    @Bean
    public CasEventRepositoryFilter jpaEventRepositoryFilter() {
        return CasEventRepositoryFilter.noOp();
    }

    @Bean
    @Autowired
    public CasEventRepository casEventRepository(@Qualifier("transactionManagerEvents") final PlatformTransactionManager transactionManager) {
        return new JpaCasEventRepository(jpaEventRepositoryFilter(), transactionManager);
    }
}
