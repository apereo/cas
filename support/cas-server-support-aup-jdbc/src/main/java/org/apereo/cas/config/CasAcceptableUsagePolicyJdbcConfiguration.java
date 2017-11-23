package org.apereo.cas.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.aup.AcceptableUsagePolicyRepository;
import org.apereo.cas.aup.JdbcAcceptableUsagePolicyRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.aup.AcceptableUsagePolicyProperties;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasAcceptableUsagePolicyJdbcConfiguration} that stores AUP decisions in a mongo database.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("casAcceptableUsagePolicyJdbcConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasAcceptableUsagePolicyJdbcConfiguration {

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private TicketRegistrySupport ticketRegistrySupport;

    @Autowired
    private CasConfigurationProperties casProperties;
    
    @RefreshScope
    @Bean
    public AcceptableUsagePolicyRepository acceptableUsagePolicyRepository() {
        final AcceptableUsagePolicyProperties.Jdbc jdbc = casProperties.getAcceptableUsagePolicy().getJdbc();
        
        if (StringUtils.isBlank(jdbc.getTableName())) {
            throw new BeanCreationException("Database table for acceptable usage policy must be specified.");
        }
        
        return new JdbcAcceptableUsagePolicyRepository(ticketRegistrySupport, 
                casProperties.getAcceptableUsagePolicy().getAupAttributeName(),
                JpaBeans.newDataSource(jdbc),
                jdbc.getTableName());
    }
}
