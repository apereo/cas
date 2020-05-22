package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigurationContext;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.JpaServiceRegistry;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServiceRegistryListener;

import lombok.val;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.beans.factory.ObjectProvider;
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
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import java.util.Collection;
import java.util.List;
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

    @Autowired
    @Qualifier("jpaBeanFactory")
    private ObjectProvider<JpaBeanFactory> jpaBeanFactory;

    @Autowired
    @Qualifier("serviceRegistryListeners")
    private ObjectProvider<Collection<ServiceRegistryListener>> serviceRegistryListeners;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @RefreshScope
    @Bean
    public JpaVendorAdapter jpaServiceVendorAdapter() {
        return jpaBeanFactory.getObject().newJpaVendorAdapter(casProperties.getJdbc());
    }

    @Bean
    public List<String> jpaServicePackagesToScan() {
        val reflections =
            new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(CentralAuthenticationService.NAMESPACE))
                .setScanners(new SubTypesScanner(false)));
        val subTypes = reflections.getSubTypesOf(AbstractRegisteredService.class);
        return subTypes.stream().map(t -> t.getPackage().getName()).collect(Collectors.toList());
    }

    @Lazy
    @Bean
    public LocalContainerEntityManagerFactoryBean serviceEntityManagerFactory() {

        val factory = jpaBeanFactory.getObject();
        val ctx = new JpaConfigurationContext(
            jpaServiceVendorAdapter(),
            "jpaServiceRegistryContext",
            jpaServicePackagesToScan(),
            dataSourceService());
        return factory.newEntityManagerFactoryBean(ctx, casProperties.getServiceRegistry().getJpa());
    }

    @Autowired
    @Bean
    public PlatformTransactionManager transactionManagerServiceReg(@Qualifier("serviceEntityManagerFactory") final EntityManagerFactory emf) {
        val mgmr = new JpaTransactionManager();
        mgmr.setEntityManagerFactory(emf);
        return mgmr;
    }

    @Bean
    @ConditionalOnMissingBean(name = "dataSourceService")
    @RefreshScope
    public DataSource dataSourceService() {
        return JpaBeans.newDataSource(casProperties.getServiceRegistry().getJpa());
    }

    @Bean
    @RefreshScope
    public ServiceRegistry jpaServiceRegistry() {
        return new JpaServiceRegistry(applicationContext, serviceRegistryListeners.getObject());
    }

    @Bean
    @ConditionalOnMissingBean(name = "jpaServiceRegistryExecutionPlanConfigurer")
    public ServiceRegistryExecutionPlanConfigurer jpaServiceRegistryExecutionPlanConfigurer() {
        return plan -> plan.registerServiceRegistry(jpaServiceRegistry());
    }
}
