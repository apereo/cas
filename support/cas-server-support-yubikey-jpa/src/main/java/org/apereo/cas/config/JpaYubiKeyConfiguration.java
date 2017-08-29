package org.apereo.cas.config;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.dao.JpaYubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigDataHolder;
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
 * This is {@link JpaYubiKeyConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("jpaYubiKeyConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = true)
public class JpaYubiKeyConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("yubiKeyAccountValidator")
    private YubiKeyAccountValidator yubiKeyAccountValidator;

    @RefreshScope
    @Bean
    public HibernateJpaVendorAdapter jpaYubiKeyVendorAdapter() {
        return JpaBeans.newHibernateJpaVendorAdapter(casProperties.getJdbc());
    }

    @RefreshScope
    @Bean
    public DataSource dataSourceYubiKey() {
        return JpaBeans.newDataSource(casProperties.getAuthn().getMfa().getYubikey().getJpa());
    }

    public List<String> jpaYubiKeyPackagesToScan() {
        return CollectionUtils.wrapList(YubiKeyAccount.class.getPackage().getName());
    }

    @Autowired
    @Bean
    public PlatformTransactionManager transactionManagerYubiKey(@Qualifier("yubiKeyEntityManagerFactory") final EntityManagerFactory emf) {
        final JpaTransactionManager mgmr = new JpaTransactionManager();
        mgmr.setEntityManagerFactory(emf);
        return mgmr;
    }

    @Lazy
    @Bean
    public LocalContainerEntityManagerFactoryBean yubiKeyEntityManagerFactory() {
        final LocalContainerEntityManagerFactoryBean bean =
                JpaBeans.newHibernateEntityManagerFactoryBean(
                        new JpaConfigDataHolder(
                                jpaYubiKeyVendorAdapter(),
                                "jpaYubiKeyRegistryContext",
                                jpaYubiKeyPackagesToScan(),
                                dataSourceYubiKey()),
                        casProperties.getAuthn().getMfa().getYubikey().getJpa());

        return bean;
    }

    @Bean
    public YubiKeyAccountRegistry yubiKeyAccountRegistry() {
        return new JpaYubiKeyAccountRegistry(yubiKeyAccountValidator);
    }
}
