package org.apereo.cas.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.aup.AcceptableUsagePolicyRepository;
import org.apereo.cas.aup.JdbcAcceptableUsagePolicyRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
@Slf4j
public class CasAcceptableUsagePolicyJdbcConfiguration {

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private TicketRegistrySupport ticketRegistrySupport;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    public DataSource acceptableUsagePolicyDataSource() {
        final var jdbc = casProperties.getAcceptableUsagePolicy().getJdbc();
        return JpaBeans.newDataSource(jdbc);
    }

    @RefreshScope
    @Bean
    public AcceptableUsagePolicyRepository acceptableUsagePolicyRepository() {
        final var jdbc = casProperties.getAcceptableUsagePolicy().getJdbc();

        if (StringUtils.isBlank(jdbc.getTableName())) {
            throw new BeanCreationException("Database table for acceptable usage policy must be specified.");
        }

        return new JdbcAcceptableUsagePolicyRepository(ticketRegistrySupport,
            casProperties.getAcceptableUsagePolicy().getAupAttributeName(),
            acceptableUsagePolicyDataSource(),
            jdbc.getTableName());
    }
}
