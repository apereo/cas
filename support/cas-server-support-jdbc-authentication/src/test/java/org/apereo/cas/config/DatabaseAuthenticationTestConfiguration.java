package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigurationContext;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.util.CollectionUtils;

import lombok.SneakyThrows;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;

/**
 * This is {@link DatabaseAuthenticationTestConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@TestConfiguration("databaseAuthenticationTestConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Lazy(false)
public class DatabaseAuthenticationTestConfiguration {
    @Value("${database.user:sa}")
    private String databaseUser;

    @Value("${database.password:}")
    private String databasePassword;

    @Value("${database.url:jdbc:hsqldb:mem:}")
    private String databaseUrl;

    @Value("${database.name:cas-authentications}")
    private String databaseName;

    @Value("${database.driverClass:org.hsqldb.jdbcDriver}")
    private String databaseDriverClassName;

    @Value("${database.dialect:org.hibernate.dialect.HSQLDialect}")
    private String databaseDialect;

    @Value("${database.hbm2ddl:create-drop}")
    private String hbm2ddl;

    @Autowired
    @Qualifier("jpaBeanFactory")
    private JpaBeanFactory jpaBeanFactory;

    @SneakyThrows
    @Bean
    public DataSource dataSource() {
        return JpaBeans.newDataSource(databaseDriverClassName, databaseUser, databasePassword, this.databaseUrl + databaseName);
    }

    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        return jpaBeanFactory.newJpaVendorAdapter();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        val ctx = new JpaConfigurationContext(
            jpaVendorAdapter(),
            "databaseAuthnContext",
            CollectionUtils.wrap("org.apereo.cas.adaptors.jdbc"),
            dataSource());

        val jpaProperties = ctx.getJpaProperties();
        jpaProperties.put("hibernate.dialect", databaseDialect);
        jpaProperties.put("hibernate.hbm2ddl.auto", this.hbm2ddl);
        jpaProperties.put("hibernate.jdbc.batch_size", 1);

        return JpaBeans.newEntityManagerFactoryBean(ctx);
    }
}
