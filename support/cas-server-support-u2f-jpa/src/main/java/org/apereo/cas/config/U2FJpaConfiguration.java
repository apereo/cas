package org.apereo.cas.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.adaptors.u2f.storage.U2FDeviceRegistration;
import org.apereo.cas.adaptors.u2f.storage.U2FDeviceRepository;
import org.apereo.cas.adaptors.u2f.storage.U2FJpaDeviceRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigDataHolder;
import org.apereo.cas.configuration.model.support.mfa.U2FMultifactorProperties;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.util.CollectionUtils;
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

/**
 * This is {@link U2FJpaConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("u2fJpaConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = true)
public class U2FJpaConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @Bean
    public HibernateJpaVendorAdapter jpaU2fVendorAdapter() {
        return JpaBeans.newHibernateJpaVendorAdapter(casProperties.getJdbc());
    }

    @RefreshScope
    @Bean
    public DataSource dataSourceU2f() {
        return JpaBeans.newDataSource(casProperties.getAuthn().getMfa().getU2f().getJpa());
    }

    public List<String> jpaU2fPackagesToScan() {
        return CollectionUtils.wrapList(U2FDeviceRegistration.class.getPackage().getName());
    }

    @Lazy
    @Bean
    public LocalContainerEntityManagerFactoryBean u2fEntityManagerFactory() {
        final LocalContainerEntityManagerFactoryBean bean =
                JpaBeans.newHibernateEntityManagerFactoryBean(
                        new JpaConfigDataHolder(
                                jpaU2fVendorAdapter(),
                                "jpaU2fRegistryContext",
                                jpaU2fPackagesToScan(),
                                dataSourceU2f()),
                        casProperties.getAuthn().getMfa().getU2f().getJpa());

        return bean;
    }

    @Autowired
    @Bean
    public PlatformTransactionManager transactionManagerU2f(@Qualifier("u2fEntityManagerFactory") final EntityManagerFactory emf) {
        final JpaTransactionManager mgmr = new JpaTransactionManager();
        mgmr.setEntityManagerFactory(emf);
        return mgmr;
    }

    @Bean
    public U2FDeviceRepository u2fDeviceRepository() {
        final U2FMultifactorProperties u2f = casProperties.getAuthn().getMfa().getU2f();
        final LoadingCache<String, String> requestStorage =
                Caffeine.newBuilder()
                        .expireAfterWrite(u2f.getExpireRegistrations(), u2f.getExpireRegistrationsTimeUnit())
                        .build(key -> StringUtils.EMPTY);
        return new U2FJpaDeviceRepository(requestStorage, u2f.getExpireRegistrations(), u2f.getExpireDevicesTimeUnit());
    }

}
