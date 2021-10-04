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
import org.apereo.cas.util.spring.BeanContainer;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

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

    @Configuration(value = "SamlIdPJpaRegisteredServiceMetadataResolverConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SamlIdPJpaRegisteredServiceMetadataResolverConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "jpaSamlRegisteredServiceMetadataResolver")
        @Autowired
        public SamlRegisteredServiceMetadataResolver jpaSamlRegisteredServiceMetadataResolver(
            final CasConfigurationProperties casProperties,
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean openSamlConfigBean) {
            val idp = casProperties.getAuthn().getSamlIdp();
            return new JpaSamlRegisteredServiceMetadataResolver(idp, openSamlConfigBean);
        }

    }

    @Configuration(value = "SamlIdPJpaRegisteredServiceMetadataDataConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SamlIdPJpaRegisteredServiceMetadataDataConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "dataSourceSamlMetadata")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public DataSource dataSourceSamlMetadata(final CasConfigurationProperties casProperties) {
            val idp = casProperties.getAuthn().getSamlIdp().getMetadata();
            return JpaBeans.newDataSource(idp.getJpa());
        }

    }

    @Configuration(value = "SamlIdPJpaRegisteredServiceMetadataEntityConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SamlIdPJpaRegisteredServiceMetadataEntityConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @Autowired
        public JpaVendorAdapter jpaSamlMetadataVendorAdapter(final CasConfigurationProperties casProperties,
                                                             @Qualifier("jpaBeanFactory")
                                                             final JpaBeanFactory jpaBeanFactory) {
            return jpaBeanFactory.newJpaVendorAdapter(casProperties.getJdbc());
        }


        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public BeanContainer<String> jpaSamlMetadataPackagesToScan() {
            return BeanContainer.of(CollectionUtils.wrapSet(SamlMetadataDocument.class.getPackage().getName()));
        }

        @Bean
        @Autowired
        public LocalContainerEntityManagerFactoryBean samlMetadataEntityManagerFactory(
            final CasConfigurationProperties casProperties,
            @Qualifier("jpaSamlMetadataVendorAdapter")
            final JpaVendorAdapter jpaSamlMetadataVendorAdapter,
            @Qualifier("dataSourceSamlMetadata")
            final DataSource dataSourceSamlMetadata,
            @Qualifier("jpaSamlMetadataPackagesToScan")
            final BeanContainer<String> jpaSamlMetadataPackagesToScan,
            @Qualifier("jpaBeanFactory")
            final JpaBeanFactory jpaBeanFactory) {
            val idp = casProperties.getAuthn().getSamlIdp().getMetadata();
            val ctx = JpaConfigurationContext.builder().jpaVendorAdapter(jpaSamlMetadataVendorAdapter)
                .persistenceUnitName("jpaSamlMetadataContext").dataSource(dataSourceSamlMetadata)
                .packagesToScan(jpaSamlMetadataPackagesToScan.toSet()).build();
            return jpaBeanFactory.newEntityManagerFactoryBean(ctx, idp.getJpa());
        }
    }

    @Configuration(value = "SamlIdPJpaRegisteredServiceMetadataTransactionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SamlIdPJpaRegisteredServiceMetadataTransactionConfiguration {
        @Autowired
        @Bean
        public PlatformTransactionManager transactionManagerSamlMetadata(
            @Qualifier("samlMetadataEntityManagerFactory")
            final EntityManagerFactory emf) {
            val mgmr = new JpaTransactionManager();
            mgmr.setEntityManagerFactory(emf);
            return mgmr;
        }
    }

    @Configuration(value = "SamlIdPJpaRegisteredServiceMetadataPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SamlIdPJpaRegisteredServiceMetadataPlanConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "jpaSamlRegisteredServiceMetadataResolutionPlanConfigurer")
        public SamlRegisteredServiceMetadataResolutionPlanConfigurer jpaSamlRegisteredServiceMetadataResolutionPlanConfigurer(
            @Qualifier("jpaSamlRegisteredServiceMetadataResolver")
            final SamlRegisteredServiceMetadataResolver jpaSamlRegisteredServiceMetadataResolver) {
            return plan -> plan.registerMetadataResolver(jpaSamlRegisteredServiceMetadataResolver);
        }
    }
}
