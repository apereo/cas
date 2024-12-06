package org.apereo.cas.config;

import org.apereo.cas.aup.AcceptableUsagePolicyRepository;
import org.apereo.cas.aup.JdbcAcceptableUsagePolicyRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.integration.transaction.PseudoTransactionManager;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;
import javax.sql.DataSource;

/**
 * This is {@link CasAcceptableUsagePolicyJdbcAutoConfiguration} that stores AUP decisions in a mongo database.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.AcceptableUsagePolicy, module = "jdbc")
@AutoConfiguration
public class CasAcceptableUsagePolicyJdbcAutoConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "acceptableUsagePolicyDataSource")
    public DataSource acceptableUsagePolicyDataSource(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(DataSource.class)
            .when(AcceptableUsagePolicyRepository.CONDITION_AUP_ENABLED.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val jdbc = casProperties.getAcceptableUsagePolicy().getJdbc();
                return JpaBeans.newDataSource(jdbc);
            })
            .otherwiseProxy()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PlatformTransactionManager jdbcAcceptableUsagePolicyTransactionManager(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("acceptableUsagePolicyDataSource")
        final DataSource acceptableUsagePolicyDataSource) {
        return BeanSupplier.of(PlatformTransactionManager.class)
            .when(AcceptableUsagePolicyRepository.CONDITION_AUP_ENABLED.given(applicationContext.getEnvironment()))
            .supply(() -> new DataSourceTransactionManager(acceptableUsagePolicyDataSource))
            .otherwise(PseudoTransactionManager::new)
            .get();
    }

    @ConditionalOnMissingBean(name = "jdbcAcceptableUsagePolicyTransactionTemplate")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public TransactionOperations jdbcAcceptableUsagePolicyTransactionTemplate(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("jdbcAcceptableUsagePolicyTransactionManager")
        final PlatformTransactionManager jdbcAcceptableUsagePolicyTransactionManager,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(TransactionOperations.class)
            .when(AcceptableUsagePolicyRepository.CONDITION_AUP_ENABLED.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val t = new TransactionTemplate(jdbcAcceptableUsagePolicyTransactionManager);
                t.setIsolationLevelName(casProperties.getAcceptableUsagePolicy().getJdbc().getIsolationLevelName());
                t.setPropagationBehaviorName(casProperties.getAcceptableUsagePolicy().getJdbc().getPropagationBehaviorName());
                return t;
            })
            .otherwiseProxy()
            .get();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public AcceptableUsagePolicyRepository acceptableUsagePolicyRepository(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("acceptableUsagePolicyDataSource")
        final DataSource acceptableUsagePolicyDataSource,
        @Qualifier("jdbcAcceptableUsagePolicyTransactionTemplate")
        final TransactionOperations jdbcAcceptableUsagePolicyTransactionTemplate,
        final CasConfigurationProperties casProperties,
        @Qualifier(TicketRegistrySupport.BEAN_NAME)
        final TicketRegistrySupport ticketRegistrySupport) {
        return BeanSupplier.of(AcceptableUsagePolicyRepository.class)
            .when(AcceptableUsagePolicyRepository.CONDITION_AUP_ENABLED.given(applicationContext.getEnvironment()))
            .supply(() -> new JdbcAcceptableUsagePolicyRepository(ticketRegistrySupport,
                casProperties.getAcceptableUsagePolicy(),
                acceptableUsagePolicyDataSource,
                jdbcAcceptableUsagePolicyTransactionTemplate))
            .otherwise(AcceptableUsagePolicyRepository::noOp)
            .get();
    }
}
