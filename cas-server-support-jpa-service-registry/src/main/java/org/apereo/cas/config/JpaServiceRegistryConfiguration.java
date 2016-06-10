package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigDataHolder;
import org.apereo.cas.services.JpaServiceRegistryDaoImpl;
import org.apereo.cas.services.ServiceRegistryDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import static org.apereo.cas.configuration.support.Beans.newEntityManagerFactoryBean;
import static org.apereo.cas.configuration.support.Beans.newHibernateJpaVendorAdapter;
import static org.apereo.cas.configuration.support.Beans.newHickariDataSource;

/**
 * This this {@link JpaServiceRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@Configuration("jpaServiceRegistryConfiguration")
public class JpaServiceRegistryConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    /**
     * Jpa vendor adapter hibernate jpa vendor adapter.
     *
     * @return the hibernate jpa vendor adapter
     */
    @RefreshScope
    @Bean
    public HibernateJpaVendorAdapter jpaServiceVendorAdapter() {
        return newHibernateJpaVendorAdapter(casProperties.getJdbc());
    }

    /**
     * Jpa packages to scan.
     *
     * @return the string [ ]
     */
    @Bean
    public String[] jpaServicePackagesToScan() {
        return new String[]{
                "org.apereo.cas.services",
                "org.apereo.cas.support.oauth.services",
                "org.apereo.cas.support.saml.services"
        };
    }

    /**
     * Entity manager factory local container.
     *
     * @return the local container entity manager factory bean
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean serviceEntityManagerFactory() {
        return newEntityManagerFactoryBean(
                new JpaConfigDataHolder(
                        jpaServiceVendorAdapter(),
                        "jpaServiceRegistryContext",
                        jpaServicePackagesToScan(),
                        dataSourceService()),
                casProperties.getJpaServiceRegistry());
    }

    /**
     * Transaction manager events jpa transaction manager.
     *
     * @param emf the emf
     * @return the jpa transaction manager
     */
    @Bean
    public JpaTransactionManager transactionManagerServiceReg(@Qualifier("serviceEntityManagerFactory")
                                                              final EntityManagerFactory emf) {
        final JpaTransactionManager mgmr = new JpaTransactionManager();
        mgmr.setEntityManagerFactory(emf);
        return mgmr;
    }

    /**
     * Data source service combo pooled data source.
     *
     * @return the combo pooled data source
     */
    @RefreshScope
    @Bean
    public DataSource dataSourceService() {
        return newHickariDataSource(casProperties.getJpaServiceRegistry());
    }

    @Bean
    public ServiceRegistryDao jpaServiceRegistryDao() {
        return new JpaServiceRegistryDaoImpl();
    }
}
