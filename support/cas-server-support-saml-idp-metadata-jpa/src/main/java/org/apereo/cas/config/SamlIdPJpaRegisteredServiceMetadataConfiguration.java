package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigurationContext;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.metadata.resolver.JpaSamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.SamlMetadataDocument;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.plan.SamlRegisteredServiceMetadataResolutionPlanConfigurer;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
import java.util.Set;

/**
 * This is {@link SamlIdPJpaRegisteredServiceMetadataConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement
@Configuration(value = "samlIdPJpaRegisteredServiceMetadataConfiguration", proxyBeanMethods = false)
public class SamlIdPJpaRegisteredServiceMetadataConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "jpaSamlRegisteredServiceMetadataResolver")
    @Autowired
    public SamlRegisteredServiceMetadataResolver jpaSamlRegisteredServiceMetadataResolver(final CasConfigurationProperties casProperties,
                                                                                          @Qualifier("openSamlConfigBean")
                                                                                          final OpenSamlConfigBean openSamlConfigBean) {
        val idp = casProperties.getAuthn().getSamlIdp();
        return new JpaSamlRegisteredServiceMetadataResolver(idp, openSamlConfigBean);
    }

    @RefreshScope
    @Bean
    @Autowired
    public JpaVendorAdapter jpaSamlMetadataVendorAdapter(final CasConfigurationProperties casProperties,
                                                         @Qualifier("jpaBeanFactory")
                                                         final JpaBeanFactory jpaBeanFactory) {
        return jpaBeanFactory.newJpaVendorAdapter(casProperties.getJdbc());
    }

    @Bean
    @ConditionalOnMissingBean(name = "dataSourceSamlMetadata")
    @RefreshScope
    @Autowired
    public DataSource dataSourceSamlMetadata(final CasConfigurationProperties casProperties) {
        val idp = casProperties.getAuthn().getSamlIdp().getMetadata();
        return JpaBeans.newDataSource(idp.getJpa());
    }

    @Bean
    @RefreshScope
    public Set<String> jpaSamlMetadataPackagesToScan() {
        return CollectionUtils.wrapSet(SamlMetadataDocument.class.getPackage().getName());
    }

    @Lazy
    @Bean
    @Autowired
    public LocalContainerEntityManagerFactoryBean samlMetadataEntityManagerFactory(final CasConfigurationProperties casProperties,
                                                                                   @Qualifier("jpaSamlMetadataVendorAdapter")
                                                                                   final JpaVendorAdapter jpaSamlMetadataVendorAdapter,
                                                                                   @Qualifier("dataSourceSamlMetadata")
                                                                                   final DataSource dataSourceSamlMetadata,
                                                                                   @Qualifier("jpaSamlMetadataPackagesToScan")
                                                                                   final Set<String> jpaSamlMetadataPackagesToScan,
                                                                                   @Qualifier("jpaBeanFactory")
                                                                                   final JpaBeanFactory jpaBeanFactory) {
        val idp = casProperties.getAuthn().getSamlIdp().getMetadata();
        val factory = jpaBeanFactory;
        val ctx = JpaConfigurationContext.builder().jpaVendorAdapter(jpaSamlMetadataVendorAdapter).persistenceUnitName("jpaSamlMetadataContext").dataSource(dataSourceSamlMetadata)
            .packagesToScan(jpaSamlMetadataPackagesToScan).build();
        return factory.newEntityManagerFactoryBean(ctx, idp.getJpa());
    }

    @Autowired
    @Bean
    public PlatformTransactionManager transactionManagerSamlMetadata(
        @Qualifier("samlMetadataEntityManagerFactory")
        final EntityManagerFactory emf) {
        val mgmr = new JpaTransactionManager();
        mgmr.setEntityManagerFactory(emf);
        return mgmr;
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "jpaSamlRegisteredServiceMetadataResolutionPlanConfigurer")
    public SamlRegisteredServiceMetadataResolutionPlanConfigurer jpaSamlRegisteredServiceMetadataResolutionPlanConfigurer(
        @Qualifier("jpaSamlRegisteredServiceMetadataResolver")
        final SamlRegisteredServiceMetadataResolver jpaSamlRegisteredServiceMetadataResolver) {
        return plan -> plan.registerMetadataResolver(jpaSamlRegisteredServiceMetadataResolver);
    }
}
