package org.apereo.cas.config;

import org.apereo.cas.aup.AcceptableUsagePolicyRepository;
import org.apereo.cas.aup.LdapAcceptableUsagePolicyRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.LdapUtils;

import lombok.val;
import org.ldaptive.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

import java.util.concurrent.ConcurrentHashMap;

/**
 * This is {@link CasAcceptableUsagePolicyLdapConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "casAcceptableUsagePolicyLdapConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cas.acceptable-usage-policy.core", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CasAcceptableUsagePolicyLdapConfiguration {

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @Autowired
    public AcceptableUsagePolicyRepository acceptableUsagePolicyRepository(final CasConfigurationProperties casProperties,
                                                                           @Qualifier(TicketRegistrySupport.BEAN_NAME)
                                                                           final TicketRegistrySupport ticketRegistrySupport) {
        val connectionFactoryList = new ConcurrentHashMap<String, ConnectionFactory>();
        val aupProperties = casProperties.getAcceptableUsagePolicy();
        aupProperties.getLdap().forEach(ldap -> connectionFactoryList.put(ldap.getLdapUrl(), LdapUtils.newLdaptiveConnectionFactory(ldap)));
        return new LdapAcceptableUsagePolicyRepository(ticketRegistrySupport, aupProperties, connectionFactoryList);
    }
}
