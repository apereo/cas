package org.apereo.cas.config;

import org.apereo.cas.aup.AcceptableUsagePolicyRepository;
import org.apereo.cas.aup.JdbcAcceptableUsagePolicyRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * This is {@link CasAcceptableUsagePolicyJdbcConfiguration} that stores AUP decisions in a mongo database.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("casAcceptableUsagePolicyJdbcConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfigureAfter(CasCoreTicketsConfiguration.class)
@ConditionalOnProperty(prefix = "cas.acceptable-usage-policy", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CasAcceptableUsagePolicyJdbcConfiguration {

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private ObjectProvider<TicketRegistrySupport> ticketRegistrySupport;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "acceptableUsagePolicyDataSource")
    public DataSource acceptableUsagePolicyDataSource() {
        val jdbc = casProperties.getAcceptableUsagePolicy().getJdbc();
        return JpaBeans.newDataSource(jdbc);
    }

    @RefreshScope
    @Bean
    public AcceptableUsagePolicyRepository acceptableUsagePolicyRepository() {
        val properties = casProperties.getAcceptableUsagePolicy();

        if (StringUtils.isBlank(properties.getJdbc().getTableName())) {
            throw new BeanCreationException("Database table for acceptable usage policy must be specified.");
        }

        if (StringUtils.isBlank(properties.getJdbc().getSqlUpdate())) {
            throw new BeanCreationException("SQL to update acceptable usage policy must be specified.");
        }

        return new JdbcAcceptableUsagePolicyRepository(ticketRegistrySupport.getObject(),
            casProperties.getAcceptableUsagePolicy(),
            acceptableUsagePolicyDataSource());
    }
}
