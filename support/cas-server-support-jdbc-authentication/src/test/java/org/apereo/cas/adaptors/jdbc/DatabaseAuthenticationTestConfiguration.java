package org.apereo.cas.adaptors.jdbc;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jpa.DatabaseProperties;
import org.apereo.cas.configuration.support.JpaBeans;

import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
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

    @Value("${database.name:cas-authentications}")
    private String databaseName;

    @Bean
    public DataSource dataSource() {
        val ds = new SimpleDriverDataSource();
        ds.setDriverClass(org.hsqldb.jdbcDriver.class);
        ds.setUsername(databaseUser);
        ds.setPassword(databasePassword);
        ds.setUrl("jdbc:hsqldb:mem:" + databaseName);
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
        bean.setPackagesToScan(CentralAuthenticationService.NAMESPACE);
        bean.setDataSource(dataSource());

        val properties = new Properties();
        properties.put("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
        properties.put("hibernate.hbm2ddl.auto", "create-drop");
        properties.put("hibernate.jdbc.batch_size", 1);
        bean.setJpaProperties(properties);
        return bean;
    }
}
