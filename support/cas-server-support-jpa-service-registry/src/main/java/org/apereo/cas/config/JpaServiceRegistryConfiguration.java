package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigDataHolder;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.JpaServiceRegistryDaoImpl;
import org.apereo.cas.services.ServiceRegistryDao;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
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
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This this {@link JpaServiceRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@Configuration("jpaServiceRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = true)
public class JpaServiceRegistryConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @Bean
    public HibernateJpaVendorAdapter jpaServiceVendorAdapter() {
        return JpaBeans.newHibernateJpaVendorAdapter(casProperties.getJdbc());
    }

    @Bean
    public List<String> jpaServicePackagesToScan() {
        final Reflections reflections =
                new Reflections(new ConfigurationBuilder()
                        .setUrls(ClasspathHelper.forPackage(CentralAuthenticationService.NAMESPACE))
                        .setScanners(new SubTypesScanner(false)));
        final Set<Class<? extends AbstractRegisteredService>> subTypes = reflections.getSubTypesOf(AbstractRegisteredService.class);
        return subTypes.stream().map(t -> t.getPackage().getName()).collect(Collectors.toList());
    }

    @Lazy
    @Bean
    public LocalContainerEntityManagerFactoryBean serviceEntityManagerFactory() {
        return JpaBeans.newHibernateEntityManagerFactoryBean(
                new JpaConfigDataHolder(
                        jpaServiceVendorAdapter(),
                        "jpaServiceRegistryContext",
                        jpaServicePackagesToScan(),
                        dataSourceService()),
                casProperties.getServiceRegistry().getJpa());
    }

    @Autowired
    @Bean
    public PlatformTransactionManager transactionManagerServiceReg(@Qualifier("serviceEntityManagerFactory") final EntityManagerFactory emf) {
        final JpaTransactionManager mgmr = new JpaTransactionManager();
        mgmr.setEntityManagerFactory(emf);
        return mgmr;
    }

    @RefreshScope
    @Bean
    public DataSource dataSourceService() {
        return JpaBeans.newDataSource(casProperties.getServiceRegistry().getJpa());
    }

    @Bean
    public ServiceRegistryDao serviceRegistryDao() {
        return new JpaServiceRegistryDaoImpl();
    }
}
