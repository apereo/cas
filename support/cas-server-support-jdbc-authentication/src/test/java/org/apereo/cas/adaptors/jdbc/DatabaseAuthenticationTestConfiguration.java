package org.apereo.cas.adaptors.jdbc;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jpa.DatabaseProperties;
import org.apereo.cas.configuration.support.JpaBeans;

import lombok.SneakyThrows;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.sql.Driver;
import java.util.Properties;

/**
 * This is {@link DatabaseAuthenticationTestConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@TestConfiguration("databaseAuthenticationTestConfiguration")
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

    @Value("${database.driverClass:org.hsqldb.jdbcDriver}")
    private String databaseDriverClassName;

    @Value("${database.dialect:org.hibernate.dialect.HSQLDialect}")
    private String databaseDialect;

    @Value("${database.hbm2ddl:create-drop}")
    private String hbm2ddl;

    @SneakyThrows
    @Bean
    public DataSource dataSource() {
        val ds = new SimpleDriverDataSource();
        ds.setDriverClass((Class<Driver>) Class.forName(databaseDriverClassName));
        ds.setUsername(databaseUser);
        ds.setPassword(databasePassword);
        ds.setUrl(this.databaseUrl + databaseName);
        return ds;
    }

    @Bean
    public HibernateJpaVendorAdapter jpaVendorAdapter() {
        val properties = new DatabaseProperties();
        properties.setGenDdl(true);
        properties.setShowSql(true);
        return JpaBeans.newHibernateJpaVendorAdapter(properties);
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        val bean = new LocalContainerEntityManagerFactoryBean();
        bean.setPersistenceUnitName("databaseAuthnContext");
        bean.setJpaVendorAdapter(jpaVendorAdapter());
        bean.setPackagesToScan("org.apereo.cas.adaptors.jdbc");
        bean.setDataSource(dataSource());

        val properties = new Properties();
        properties.put("hibernate.dialect", databaseDialect);
        properties.put("hibernate.hbm2ddl.auto", this.hbm2ddl);
        properties.put("hibernate.jdbc.batch_size", 1);
        bean.setJpaProperties(properties);
        return bean;
    }
}
