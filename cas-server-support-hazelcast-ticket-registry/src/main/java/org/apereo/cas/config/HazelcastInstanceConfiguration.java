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
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
        final HazelcastProperties.Cluster cluster = casProperties.getTicket().getRegistry().getHazelcast().getCluster();
                
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
            config.setProperty("hazelcast.prefer.ipv4.stack", String.valueOf(cluster.isIpv4Enabled()));
            
            //TCP config
            final TcpIpConfig tcpIpConfig = new TcpIpConfig()
                    .setEnabled(cluster.isTcpipEnabled())
                    .setMembers(cluster.getMembers())
                    .setConnectionTimeoutSeconds(cluster.getTimeout());

            //Multicast config
            final MulticastConfig multicastConfig = new MulticastConfig().setEnabled(cluster.isMulticastEnabled());
            if (cluster.isMulticastEnabled()) {
                multicastConfig.setMulticastGroup(cluster.getMulticastGroup());
                multicastConfig.setMulticastPort(cluster.getMulticastPort());
                
                final Set<String> trustedInterfaces = StringUtils.commaDelimitedListToSet(cluster.getMulticastTrustedInterfaces());
                if (!trustedInterfaces.isEmpty()) {
                    multicastConfig.setTrustedInterfaces(trustedInterfaces);
                }
                multicastConfig.setMulticastTimeoutSeconds(cluster.getMulticastTimeout());
                multicastConfig.setMulticastTimeToLive(cluster.getMulticastTimeToLive());
            }

            //Join config
            final JoinConfig joinConfig = new JoinConfig()
                    .setMulticastConfig(multicastConfig)
                    .setTcpIpConfig(tcpIpConfig);
            
            //Network config
            final NetworkConfig networkConfig = new NetworkConfig()
                    .setPort(cluster.getPort())
                    .setPortAutoIncrement(cluster.isPortAutoIncrement())
                    .setJoin(joinConfig);

            //Map config
            final MapConfig mapConfig = new MapConfig().setName(casProperties.getTicket()
                    .getRegistry().getHazelcast().getMapName())
                    .setMaxIdleSeconds(casProperties.getTicket().getTgt().getMaxTimeToLiveInSeconds())
                    .setBackupCount(cluster.getBackupCount())
                    .setAsyncBackupCount(cluster.getAsyncBackupCount())
                    .setEvictionPolicy(EvictionPolicy.valueOf(
                            cluster.getEvictionPolicy()))
                    .setEvictionPercentage(cluster.getEvictionPercentage())
                    .setMaxSizeConfig(new MaxSizeConfig()
                            .setMaxSizePolicy(MaxSizeConfig.MaxSizePolicy.valueOf(
                                    cluster.getMaxSizePolicy()))
                            .setSize(cluster.getMaxHeapSizePercentage()));

            final Map<String, MapConfig> mapConfigs = new HashMap<>();
            mapConfigs.put(casProperties.getTicket().getRegistry().getHazelcast().getMapName(), mapConfig);

            //Finally aggregate all those config into the main Config
            config.setMapConfigs(mapConfigs).setNetworkConfig(networkConfig);
        }
        //Add additional default config properties regardless of the configuration source
        return config.setInstanceName(cluster.getInstanceName())
                .setProperty(HazelcastProperties.LOGGING_TYPE_PROP,
                        cluster.getLoggingType())
                .setProperty(HazelcastProperties.MAX_HEARTBEAT_SECONDS_PROP,
                        String.valueOf(cluster.getMaxNoHeartbeatSeconds()));
    }
}
