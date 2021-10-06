package org.apereo.cas.config;

import org.apereo.cas.aup.AcceptableUsagePolicyRepository;
import org.apereo.cas.aup.CouchbaseAcceptableUsagePolicyRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.couchbase.core.CouchbaseClientFactory;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasAcceptableUsagePolicyCouchbaseConfiguration} that stores AUP decisions in a mongo database.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cas.acceptable-usage-policy.core", name = "enabled", havingValue = "true", matchIfMissing = true)
@Configuration(value = "casAcceptableUsagePolicyCouchbaseConfiguration", proxyBeanMethods = false)
public class CasAcceptableUsagePolicyCouchbaseConfiguration {

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @Autowired
    public CouchbaseClientFactory aupCouchbaseClientFactory(final CasConfigurationProperties casProperties) {
        val cb = casProperties.getAcceptableUsagePolicy().getCouchbase();
        return new CouchbaseClientFactory(cb);
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @Autowired
    public AcceptableUsagePolicyRepository acceptableUsagePolicyRepository(final CasConfigurationProperties casProperties,
                                                                           @Qualifier("aupCouchbaseClientFactory")
                                                                           final CouchbaseClientFactory aupCouchbaseClientFactory,
                                                                           @Qualifier(TicketRegistrySupport.BEAN_NAME)
                                                                           final TicketRegistrySupport ticketRegistrySupport) {
        return new CouchbaseAcceptableUsagePolicyRepository(ticketRegistrySupport, casProperties.getAcceptableUsagePolicy(), aupCouchbaseClientFactory);
    }
}
