package org.apereo.cas.config;

import org.apereo.cas.aup.AcceptableUsagePolicyRepository;
import org.apereo.cas.aup.JdbcAcceptableUsagePolicyRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

/**
 * This is {@link CasAcceptableUsagePolicyJdbcConfiguration} that stores AUP decisions in a mongo database.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration(value = "casAcceptableUsagePolicyJdbcConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfigureAfter(CasCoreTicketsConfiguration.class)
@ConditionalOnProperty(prefix = "cas.acceptable-usage-policy.core", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CasAcceptableUsagePolicyJdbcConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "acceptableUsagePolicyDataSource")
    @Autowired
    public DataSource acceptableUsagePolicyDataSource(final CasConfigurationProperties casProperties) {
        val jdbc = casProperties.getAcceptableUsagePolicy().getJdbc();
        return JpaBeans.newDataSource(jdbc);
    }

    @Bean
    @Autowired
    public PlatformTransactionManager jdbcAcceptableUsagePolicyTransactionManager(
        @Qualifier("acceptableUsagePolicyDataSource")
        final DataSource acceptableUsagePolicyDataSource) {
        return new DataSourceTransactionManager(acceptableUsagePolicyDataSource);
    }

    @ConditionalOnMissingBean(name = "jdbcAcceptableUsagePolicyTransactionTemplate")
    @Bean
    @Autowired
    public TransactionTemplate jdbcAcceptableUsagePolicyTransactionTemplate(
        @Qualifier("jdbcAcceptableUsagePolicyTransactionManager")
        final PlatformTransactionManager jdbcAcceptableUsagePolicyTransactionManager,
        final CasConfigurationProperties casProperties) {
        val t = new TransactionTemplate(jdbcAcceptableUsagePolicyTransactionManager);
        t.setIsolationLevelName(casProperties.getAcceptableUsagePolicy().getJdbc().getIsolationLevelName());
        t.setPropagationBehaviorName(casProperties.getAcceptableUsagePolicy().getJdbc().getPropagationBehaviorName());
        return t;
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @Autowired
    public AcceptableUsagePolicyRepository acceptableUsagePolicyRepository(
        @Qualifier("acceptableUsagePolicyDataSource")
        final DataSource acceptableUsagePolicyDataSource,
        @Qualifier("jdbcAcceptableUsagePolicyTransactionTemplate")
        final TransactionTemplate jdbcAcceptableUsagePolicyTransactionTemplate,
        final CasConfigurationProperties casProperties,
        @Qualifier(TicketRegistrySupport.BEAN_NAME)
        final TicketRegistrySupport ticketRegistrySupport) {
        return new JdbcAcceptableUsagePolicyRepository(ticketRegistrySupport,
            casProperties.getAcceptableUsagePolicy(),
            acceptableUsagePolicyDataSource,
            jdbcAcceptableUsagePolicyTransactionTemplate);
    }
}
