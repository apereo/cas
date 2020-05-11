package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigurationContext;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.uma.ticket.resource.ResourceSet;
import org.apereo.cas.uma.ticket.resource.repository.ResourceSetRepository;
import org.apereo.cas.uma.ticket.resource.repository.impl.JpaResourceSetRepository;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
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

import java.util.List;

/**
 * This is {@link CasOAuthUmaJpaConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Configuration("casOAuthUmaJpaConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfigureBefore(CasOAuthUmaConfiguration.class)
@EnableTransactionManagement(proxyTargetClass = true)
@ConditionalOnProperty(name = "cas.authn.uma.resource-set.jpa.url")
public class CasOAuthUmaJpaConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("jpaBeanFactory")
    private ObjectProvider<JpaBeanFactory> jpaBeanFactory;

    @RefreshScope
    @Bean
    public JpaVendorAdapter jpaUmaVendorAdapter() {
        return jpaBeanFactory.getObject().newJpaVendorAdapter(casProperties.getJdbc());
    }

    @Bean
    public List<String> jpaUmaPackagesToScan() {
        return CollectionUtils.wrapList(ResourceSet.class.getPackage().getName());
    }

    @Lazy
    @Bean
    public LocalContainerEntityManagerFactoryBean umaEntityManagerFactory() {
        val factory = jpaBeanFactory.getObject();
        val ctx = new JpaConfigurationContext(
            jpaUmaVendorAdapter(),
            getClass().getSimpleName(),
            jpaUmaPackagesToScan(),
            dataSourceUma());
        return factory.newEntityManagerFactoryBean(ctx, casProperties.getAuthn().getUma().getResourceSet().getJpa());
    }

    @Autowired
    @Bean
    public PlatformTransactionManager umaTransactionManager(@Qualifier("umaEntityManagerFactory") final EntityManagerFactory emf) {
        val mgmr = new JpaTransactionManager();
        mgmr.setEntityManagerFactory(emf);
        return mgmr;
    }

    @Bean
    @ConditionalOnMissingBean(name = "dataSourceUma")
    @RefreshScope
    public DataSource dataSourceUma() {
        return JpaBeans.newDataSource(casProperties.getAuthn().getUma().getResourceSet().getJpa());
    }

    @Bean
    public ResourceSetRepository umaResourceSetRepository() {
        return new JpaResourceSetRepository();
    }

}
