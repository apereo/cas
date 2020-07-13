package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.hz.HazelcastConfigurationFactory;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.registry.HazelcastTicketRegistry;
import org.apereo.cas.ticket.registry.NoOpTicketRegistryCleaner;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistryCleaner;
import org.apereo.cas.util.CoreTicketUtils;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring's Java configuration component for {@code HazelcastInstance} that is consumed and used by
 * {@link HazelcastTicketRegistry}.
 * <p>
 * This configuration class has the smarts to choose the configuration source for the {@link HazelcastInstance}
 * that it produces by either loading the native hazelcast XML config file from a resource location
 * or it creates the {@link HazelcastInstance} programmatically
 * with a handful properties and their defaults (if not set) that it exposes to CAS deployers.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 4.2.0
 */
@Configuration("hazelcastTicketRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class HazelcastTicketRegistryConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("ticketCatalog")
    private ObjectProvider<TicketCatalog> ticketCatalog;

    @Bean
    @RefreshScope
    public TicketRegistry ticketRegistry() {
        val hz = casProperties.getTicket().getRegistry().getHazelcast();
        val hazelcastInstance = casTicketRegistryHazelcastInstance();
        val r = new HazelcastTicketRegistry(hazelcastInstance, ticketCatalog.getObject(), hz.getPageSize());
        r.setCipherExecutor(CoreTicketUtils.newTicketRegistryCipherExecutor(hz.getCrypto(), "hazelcast"));
        return r;
    }

    @ConditionalOnMissingBean(name = "casTicketRegistryHazelcastInstance")
    @Bean(destroyMethod = "shutdown")
    public HazelcastInstance casTicketRegistryHazelcastInstance() {
        val hz = casProperties.getTicket().getRegistry().getHazelcast();
        LOGGER.debug("Creating Hazelcast instance for members [{}]", hz.getCluster().getMembers());
        val hazelcastInstance = Hazelcast.newHazelcastInstance(HazelcastConfigurationFactory.build(hz));
        val catalog = ticketCatalog.getObject();
        catalog.findAll()
            .stream()
            .map(TicketDefinition::getProperties)
            .peek(p -> LOGGER.debug("Created Hazelcast map configuration for [{}]", p))
            .map(p -> HazelcastConfigurationFactory.buildMapConfig(hz, p.getStorageName(), p.getStorageTimeout()))
            .forEach(m -> hazelcastInstance.getConfig().addMapConfig(m));
        return hazelcastInstance;
    }


    @Bean
    public TicketRegistryCleaner ticketRegistryCleaner() {
        return NoOpTicketRegistryCleaner.getInstance();
    }
}
