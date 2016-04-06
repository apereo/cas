package org.jasig.cas.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.session.hazelcast.config.annotation.web.http.EnableHazelcastHttpSession;

import java.net.URL;

/**
 * This is {@link HazelcastSessionConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RefreshScope
@Configuration("hazelcastSessionConfiguration")
@EnableHazelcastHttpSession
public class HazelcastSessionConfiguration {
    
    @Value("${webflow.session.hz.location:}")
    private Resource configLocation;

    /**
     * Hazelcast instance that is used by the spring session
     * repository to broadcast session events. The name
     * of this bean must be left untouched.
     *
     * @return the hazelcast instance
     */
    @Bean(name="hazelcastInstance")
    public HazelcastInstance hazelcastInstance() {
        try {
            final URL configUrl = this.configLocation.getURL();
            final Config config = new XmlConfigBuilder(this.configLocation.getInputStream()).build();
            config.setConfigurationUrl(configUrl);
            config.setInstanceName(this.getClass().getSimpleName())
                    .setProperty("hazelcast.logging.type", "slf4j")
                    .setProperty("hazelcast.max.no.heartbeat.seconds", "5");
            return Hazelcast.newHazelcastInstance(config);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
