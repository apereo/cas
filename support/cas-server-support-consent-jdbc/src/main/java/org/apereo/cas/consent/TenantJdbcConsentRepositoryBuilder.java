package org.apereo.cas.consent;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.consent.JpaConsentProperties;
import org.apereo.cas.configuration.support.ConfigurationPropertiesBindingContext;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.jpa.JpaConfigurationContext;
import org.apereo.cas.multitenancy.TenantDefinition;
import org.apereo.cas.util.spring.beans.BeanContainer;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * This is {@link TenantJdbcConsentRepositoryBuilder}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@RequiredArgsConstructor
public class TenantJdbcConsentRepositoryBuilder implements TenantConsentRepositoryBuilder {
    private final BeanContainer<String> jpaConsentPackagesToScan;
    private final JpaBeanFactory jpaBeanFactory;
    
    @Override
    public List<ConsentRepository> buildInternal(
        final TenantDefinition tenantDefinition,
        final ConfigurationPropertiesBindingContext<CasConfigurationProperties> bindingContext) throws Exception {
        if (bindingContext.containsBindingFor(JpaConsentProperties.class)) {
            val dataSource = JpaBeans.newDataSource(bindingContext.value().getConsent().getJpa());
            val ctx = JpaConfigurationContext.builder()
                .jpaVendorAdapter(jpaBeanFactory.newJpaVendorAdapter(bindingContext.value().getJdbc()))
                .persistenceUnitName("jpaConsentContext")
                .dataSource(dataSource)
                .packagesToScan(jpaConsentPackagesToScan.toSet())
                .build();
            val emf = jpaBeanFactory.newEntityManagerFactoryBean(ctx,
                bindingContext.value().getConsent().getJpa()).getObject();
            Objects.requireNonNull(emf);
            val entityManager = SharedEntityManagerCreator.createSharedEntityManager(emf);
            val transactionManager = new JpaTransactionManager();
            transactionManager.setEntityManagerFactory(emf);
            
            val repository = new JpaConsentRepository(entityManager,
                dataSource, new TransactionTemplate(transactionManager));
            return List.of(repository.markDisposable());
        }
        return List.of();
    }
}
