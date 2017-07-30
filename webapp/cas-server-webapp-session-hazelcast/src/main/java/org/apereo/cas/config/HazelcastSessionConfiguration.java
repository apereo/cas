package org.apereo.cas.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
@Configuration("hazelcastSessionConfiguration")
@EnableHazelcastHttpSession
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class HazelcastSessionConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    /**
     * Hazelcast instance that is used by the spring session
     * repository to broadcast session events. The name
     * of this bean must be left untouched.
     *
     * @return the hazelcast instance
     */
    @Bean
    public HazelcastInstance hazelcastInstance() {
        final Resource hzConfigResource = casProperties.getWebflow().getSession().getHzLocation();
        try {
            final URL configUrl = hzConfigResource.getURL();
            final Config config = new XmlConfigBuilder(hzConfigResource.getInputStream()).build();
            config.setConfigurationUrl(configUrl);
            config.setInstanceName(this.getClass().getSimpleName())
                    .setProperty("hazelcast.logging.type", "slf4j")
                    .setProperty("hazelcast.max.no.heartbeat.seconds", "300");
            return Hazelcast.newHazelcastInstance(config);
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
