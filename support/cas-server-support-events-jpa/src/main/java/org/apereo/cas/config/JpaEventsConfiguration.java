package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigDataHolder;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.hibernate.HibernateBeans;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.CasEventRepositoryFilter;
import org.apereo.cas.support.events.jpa.JpaCasEvent;
import org.apereo.cas.support.events.jpa.JpaCasEventRepository;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
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
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @Bean
    public HibernateJpaVendorAdapter jpaEventVendorAdapter() {
        return HibernateBeans.newHibernateJpaVendorAdapter(casProperties.getJdbc());
    }

    @Bean
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

        return HibernateBeans.newHibernateEntityManagerFactoryBean(
            new JpaConfigDataHolder(
                jpaEventVendorAdapter(),
                "jpaEventRegistryContext",
                jpaEventPackagesToScan(),
                dataSourceEvent()),
            casProperties.getEvents().getJpa(),
            applicationContext);
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
