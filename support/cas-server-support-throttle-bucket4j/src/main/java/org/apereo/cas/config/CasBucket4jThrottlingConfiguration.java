package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.throttle.ThrottledRequestExecutor;
import org.apereo.cas.web.Bucket4jThrottledRequestExecutor;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasBucket4jThrottlingConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Configuration(value = "casBucket4jThrottlingConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasBucket4jThrottlingConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    public ThrottledRequestExecutor throttledRequestExecutor() {
        val throttle = casProperties.getAuthn().getThrottle();
        return new Bucket4jThrottledRequestExecutor(throttle.getBucket4j());
    }
}
