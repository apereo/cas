package org.apereo.cas.config;

import org.apereo.cas.bucket4j.consumer.BucketConsumer;
import org.apereo.cas.bucket4j.producer.BucketProducer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.throttle.ThrottledRequestExecutor;
import org.apereo.cas.web.Bucket4jThrottledRequestExecutor;

import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasBucket4jThrottlingConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Configuration(value = "CasBucket4jThrottlingConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cas.authn.throttle.bucket4j", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CasBucket4jThrottlingConfiguration {

    @Bean
    public ThrottledRequestExecutor throttledRequestExecutor(
        @Qualifier("bucket4jThrottledRequestConsumer")
        final BucketConsumer bucket4jThrottledRequestConsumer) {
        return new Bucket4jThrottledRequestExecutor(bucket4jThrottledRequestConsumer);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "bucket4jThrottledRequestConsumer")
    public BucketConsumer bucket4jThrottledRequestConsumer(final CasConfigurationProperties casProperties) {
        val throttle = casProperties.getAuthn().getThrottle();
        return BucketProducer.builder().properties(throttle.getBucket4j()).build().produce();
    }
}
