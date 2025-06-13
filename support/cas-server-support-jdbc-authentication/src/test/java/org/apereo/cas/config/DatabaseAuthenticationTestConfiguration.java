package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.hibernate.CasHibernatePersistenceProvider;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.jpa.JpaConfigurationContext;
import org.apereo.cas.jpa.JpaPersistenceProviderContext;
import org.apereo.cas.util.CollectionUtils;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.orm.jpa.JpaVendorAdapter;
import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 * This is {@link DatabaseAuthenticationTestConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@TestConfiguration(value = "databaseAuthenticationTestConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class DatabaseAuthenticationTestConfiguration {
    @Value("${database.user:sa}")
    private String databaseUser;

    @Value("${database.password:}")
    private String databasePassword;

    @Value("${database.url:jdbc:hsqldb:mem:}")
    private String databaseUrl;

    @Value("${database.name:cas-authentications}")
    private String databaseName;

    @Value("${database.driver-class:org.hsqldb.jdbcDriver}")
    private String databaseDriverClassName;

    @Value("${database.dialect:org.hibernate.dialect.HSQLDialect}")
    private String databaseDialect;

    @Value("${database.hbm2ddl:create-drop}")
    private String hbm2ddl;

    @Autowired
    @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME)
    private JpaBeanFactory jpaBeanFactory;

    @Autowired
    @Qualifier("persistenceProviderContext")
    private JpaPersistenceProviderContext persistenceProviderContext;

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public DataSource dataSource() {
        return JpaBeans.newDataSource(databaseDriverClassName, databaseUser, databasePassword, this.databaseUrl + databaseName);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public JpaVendorAdapter jpaVendorAdapter() {
        return jpaBeanFactory.newJpaVendorAdapter();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public EntityManagerFactory entityManagerFactory(
        @Qualifier("jpaVendorAdapter")
        final JpaVendorAdapter jpaVendorAdapter,
        @Qualifier("dataSource")
        final DataSource dataSource) {
        val ctx = JpaConfigurationContext.builder()
            .jpaVendorAdapter(jpaVendorAdapter)
            .persistenceUnitName("databaseAuthnContext")
            .dataSource(dataSource)
            .persistenceProvider(new CasHibernatePersistenceProvider(persistenceProviderContext))
            .packagesToScan(CollectionUtils.wrapSet("org.apereo.cas.adaptors.jdbc"))
            .build();
        val jpaProperties = ctx.getJpaProperties();
        jpaProperties.put("hibernate.dialect", databaseDialect);
        jpaProperties.put("hibernate.hbm2ddl.auto", this.hbm2ddl);
        jpaProperties.put("hibernate.jdbc.batch_size", 1);

        val factory = JpaBeans.newEntityManagerFactoryBean(ctx);
        factory.afterPropertiesSet();
        return factory.getObject();
    }
}
