package org.apereo.cas.config;

import com.warrenstrange.googleauth.ICredentialRepository;
import org.apereo.cas.adaptors.gauth.JpaGoogleAuthenticatorAccountRegistry;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigDataHolder;
import org.apereo.cas.configuration.support.Beans;
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

/**
 * This is {@link GoogleAuthentiacatorJpaConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("googleAuthentiacatorJpaConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = true)
public class GoogleAuthentiacatorJpaConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;
    
    @RefreshScope
    @Bean
    public HibernateJpaVendorAdapter jpaEventVendorAdapter() {
        return Beans.newHibernateJpaVendorAdapter(casProperties.getJdbc());
    }
    
    @RefreshScope
    @Bean
    public DataSource dataSourceEvent() {
        return Beans.newHickariDataSource(casProperties.getAuthn().getMfa().getGauth().getJpa().getDatabase());
    }
    
    @Bean
    public String[] jpaEventPackagesToScan() {
        return new String[]{"org.apereo.cas.adaptors.gauth"};
    }
    
    @Lazy
    @Bean
    public LocalContainerEntityManagerFactoryBean googleAuthenticatorEntityManagerFactory() {
        final LocalContainerEntityManagerFactoryBean bean =
                Beans.newEntityManagerFactoryBean(
                        new JpaConfigDataHolder(
                                jpaEventVendorAdapter(),
                                "jpaGoogleAuthenticatorContext",
                                jpaEventPackagesToScan(),
                                dataSourceEvent()),
                        casProperties.getAuthn().getMfa().getGauth().getJpa().getDatabase());

        bean.getJpaPropertyMap().put("hibernate.enable_lazy_load_no_trans", Boolean.TRUE);
        return bean;
    }
    
    @Autowired
    @Bean
    public PlatformTransactionManager transactionManagerGoogleAuthenticator(
            @Qualifier("googleAuthenticatorEntityManagerFactory") final EntityManagerFactory emf) {
        final JpaTransactionManager mgmr = new JpaTransactionManager();
        mgmr.setEntityManagerFactory(emf);
        return mgmr;
    }


    @Bean
    public ICredentialRepository googleAuthenticatorAccountRegistry() {
        return new JpaGoogleAuthenticatorAccountRegistry();
    }
}
