package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;

import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.HazelcastInstanceFactory;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.hazelcast.config.annotation.web.http.EnableHazelcastHttpSession;

/**
 * This is {@link HazelcastSessionConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "hazelcastSessionConfiguration", proxyBeanMethods = false)
@EnableHazelcastHttpSession
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class HazelcastSessionConfiguration {

    /**
     * Hazelcast instance that is used by the spring session
     * repository to broadcast session events. The name
     * of this bean must be left untouched.
     *
     * @param casProperties the cas properties
     * @return the hazelcast instance
     * @throws Exception the exception
     */
    @Bean(destroyMethod = "shutdown")
    @Autowired
    public HazelcastInstance hazelcastInstance(final CasConfigurationProperties casProperties) throws Exception {
        val hzConfigResource = casProperties.getWebflow().getSession().getHzLocation();
        val configUrl = hzConfigResource.getURL();
        val config = new XmlConfigBuilder(hzConfigResource.getInputStream()).build();
        config.setConfigurationUrl(configUrl);
        config.setInstanceName(getClass().getSimpleName()).setProperty("hazelcast.logging.type", "slf4j").setProperty("hazelcast.max.no.heartbeat.seconds", "300");
        return HazelcastInstanceFactory.getOrCreateHazelcastInstance(config);
    }

}
