package org.apereo.cas.config;

import com.google.common.base.Throwables;
import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizeConfig;
import com.hazelcast.config.MulticastConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.hazelcast.HazelcastProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.ticket.registry.HazelcastTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring's Java configuration component for <code>HazelcastInstance</code> that is consumed and used by
 * {@link HazelcastTicketRegistry}.
 * <p>
 * This configuration class has the smarts to choose the configuration source for the {@link HazelcastInstance}
 * that it produces by either loading the native hazelcast XML config file from a resource location indicated by
 * <code>hz.config.location</code> property or if that property is
 * not set nor a valid location, creates HazelcastInstance programmatically
 * with a handful properties and their defaults (if not set) that it exposes to CAS deployers.
 *
 * @author Dmitriy Kopylenko
 * @since 4.2.0
 */
@Configuration("hazelcastInstanceConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class HazelcastInstanceConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;
    
    
    @Bean(name = {"hazelcastTicketRegistry", "ticketRegistry"})
    @RefreshScope
    public TicketRegistry hazelcastTicketRegistry() {
        final HazelcastTicketRegistry r = new HazelcastTicketRegistry(hazelcast(),
                casProperties.getTicket().getRegistry().getHazelcast().getMapName(),
                casProperties.getTicket().getRegistry().getHazelcast().getPageSize());
        r.setCipherExecutor(Beans.newTicketRegistryCipherExecutor(
                casProperties.getTicket().getRegistry().getHazelcast().getCrypto()));
        return r;
    }
    
    @Bean
    public HazelcastInstance hazelcast() {
        return Hazelcast.newHazelcastInstance(getConfig());
    }

    /**
     * Get Hazelcast <code>Config</code> instance.
     *
     * @return Hazelcast Config
     * @throws IOException if parsing of hazelcast xml configuration fails
     */
    private Config getConfig() {
        final Config config;
        if (casProperties.getTicket().getRegistry().getHazelcast().getConfigLocation() != null
                && casProperties.getTicket().getRegistry().getHazelcast().getConfigLocation().exists()) {

            try {
                final URL configUrl = casProperties.getTicket().getRegistry().getHazelcast().getConfigLocation().getURL();
                config = new XmlConfigBuilder(casProperties.getTicket()
                        .getRegistry().getHazelcast().getConfigLocation().getInputStream()).build();
                config.setConfigurationUrl(configUrl);
            } catch (final Exception e) {
                throw Throwables.propagate(e);
            }

        } else {
            //No config location, so do a default config programmatically with handful of properties exposed by CAS
            config = new Config();
            //TCP config
            final TcpIpConfig tcpIpConfig = new TcpIpConfig()
                    .setEnabled(casProperties.getTicket().getRegistry().getHazelcast().getCluster().isTcpipEnabled())
                    .setMembers(casProperties.getTicket().getRegistry().getHazelcast().getCluster().getMembers());

            //Multicast config
            final MulticastConfig multicastConfig = new MulticastConfig()
                    .setEnabled(casProperties.getTicket().getRegistry().getHazelcast().getCluster().isMulticastEnabled());

            //Join config
            final JoinConfig joinConfig = new JoinConfig()
                    .setMulticastConfig(multicastConfig)
                    .setTcpIpConfig(tcpIpConfig);
            
            //Network config
            final NetworkConfig networkConfig = new NetworkConfig()
                    .setPort(casProperties.getTicket().getRegistry().getHazelcast().getCluster().getPort())
                    .setPortAutoIncrement(casProperties.getTicket().getRegistry().getHazelcast().getCluster().isPortAutoIncrement())
                    .setJoin(joinConfig);

            //Map config
            final MapConfig mapConfig = new MapConfig().setName(casProperties.getTicket()
                    .getRegistry().getHazelcast().getMapName())
                    .setMaxIdleSeconds(casProperties.getTicket().getTgt().getMaxTimeToLiveInSeconds())
                    .setBackupCount(casProperties.getTicket().getRegistry().getHazelcast().getCluster().getBackupCount())
                    .setAsyncBackupCount(casProperties.getTicket().getRegistry().getHazelcast().getCluster().getAsyncBackupCount())
                    .setEvictionPolicy(EvictionPolicy.valueOf(
                            casProperties.getTicket().getRegistry().getHazelcast().getCluster().getEvictionPolicy()))
                    .setEvictionPercentage(casProperties.getTicket().getRegistry().getHazelcast().getCluster().getEvictionPercentage())
                    .setMaxSizeConfig(new MaxSizeConfig()
                            .setMaxSizePolicy(MaxSizeConfig.MaxSizePolicy.valueOf(
                                    casProperties.getTicket().getRegistry().getHazelcast().getCluster().getMaxSizePolicy()))
                            .setSize(casProperties.getTicket().getRegistry().getHazelcast().getCluster().getMaxHeapSizePercentage()));

            final Map<String, MapConfig> mapConfigs = new HashMap<>();
            mapConfigs.put(casProperties.getTicket().getRegistry().getHazelcast().getMapName(), mapConfig);

            //Finally aggregate all those config into the main Config
            config.setMapConfigs(mapConfigs).setNetworkConfig(networkConfig);
        }
        //Add additional default config properties regardless of the configuration source
        return config.setInstanceName(casProperties.getTicket().getRegistry().getHazelcast().getCluster().getInstanceName())
                .setProperty(HazelcastProperties.LOGGING_TYPE_PROP,
                        casProperties.getTicket().getRegistry().getHazelcast().getCluster().getLoggingType())
                .setProperty(HazelcastProperties.MAX_HEARTBEAT_SECONDS_PROP,
                        String.valueOf(casProperties.getTicket().getRegistry().getHazelcast().getCluster().getMaxNoHeartbeatSeconds()));
    }
}
