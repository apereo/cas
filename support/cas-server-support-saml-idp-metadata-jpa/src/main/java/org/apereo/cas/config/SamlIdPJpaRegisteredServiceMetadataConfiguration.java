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
import org.springframework.beans.factory.ObjectProvider;
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

import java.util.List;

/**
 * This is {@link SamlIdPJpaRegisteredServiceMetadataConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("samlIdPJpaRegisteredServiceMetadataConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = true)
public class SamlIdPJpaRegisteredServiceMetadataConfiguration {

    @Autowired
    @Qualifier("jpaBeanFactory")
    private ObjectProvider<JpaBeanFactory> jpaBeanFactory;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("shibboleth.OpenSAMLConfig")
    private ObjectProvider<OpenSamlConfigBean> openSamlConfigBean;
    
    @Bean
    public SamlRegisteredServiceMetadataResolver jpaSamlRegisteredServiceMetadataResolver() {
        val idp = casProperties.getAuthn().getSamlIdp();
        return new JpaSamlRegisteredServiceMetadataResolver(idp, openSamlConfigBean.getObject());
    }

    @RefreshScope
    @Bean
    public JpaVendorAdapter jpaSamlMetadataVendorAdapter() {
        return jpaBeanFactory.getObject().newJpaVendorAdapter(casProperties.getJdbc());
    }

    @Bean
    @ConditionalOnMissingBean(name = "dataSourceSamlMetadata")
    @RefreshScope
    public DataSource dataSourceSamlMetadata() {
        val idp = casProperties.getAuthn().getSamlIdp().getMetadata();
        return JpaBeans.newDataSource(idp.getJpa());
    }

    @Bean
    public List<String> jpaSamlMetadataPackagesToScan() {
        return CollectionUtils.wrapList(SamlMetadataDocument.class.getPackage().getName());
    }

    @Lazy
    @Bean
    public LocalContainerEntityManagerFactoryBean samlMetadataEntityManagerFactory() {
        val idp = casProperties.getAuthn().getSamlIdp().getMetadata();

        val factory = jpaBeanFactory.getObject();
        val ctx = new JpaConfigurationContext(
            jpaSamlMetadataVendorAdapter(),
            "jpaSamlMetadataContext",
            jpaSamlMetadataPackagesToScan(),
            dataSourceSamlMetadata());
        return factory.newEntityManagerFactoryBean(ctx, idp.getJpa());
    }

    @Autowired
    @Bean
    public PlatformTransactionManager transactionManagerSamlMetadata(
        @Qualifier("samlMetadataEntityManagerFactory") final EntityManagerFactory emf) {
        val mgmr = new JpaTransactionManager();
        mgmr.setEntityManagerFactory(emf);
        return mgmr;
    }

    @Bean
    @ConditionalOnMissingBean(name = "jpaSamlRegisteredServiceMetadataResolutionPlanConfigurer")
    public SamlRegisteredServiceMetadataResolutionPlanConfigurer jpaSamlRegisteredServiceMetadataResolutionPlanConfigurer() {
        return plan -> plan.registerMetadataResolver(jpaSamlRegisteredServiceMetadataResolver());
    }

}
