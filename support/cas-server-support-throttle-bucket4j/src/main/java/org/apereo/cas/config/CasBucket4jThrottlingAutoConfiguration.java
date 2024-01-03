package org.apereo.cas.config;

import org.apereo.cas.bucket4j.consumer.BucketConsumer;
import org.apereo.cas.bucket4j.consumer.DefaultBucketConsumer;
import org.apereo.cas.bucket4j.producer.BucketStore;
import org.apereo.cas.bucket4j.producer.InMemoryBucketStore;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.throttle.ThrottledRequestExecutor;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.Bucket4jThrottledRequestExecutor;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasBucket4jThrottlingAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Throttling, module = "bucket4j")
@AutoConfiguration
public class CasBucket4jThrottlingAutoConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.throttle.bucket4j.enabled")
        .isTrue().evenIfMissing();

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public ThrottledRequestExecutor throttledRequestExecutor(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("bucket4jThrottledRequestConsumer")
        final BucketConsumer bucket4jThrottledRequestConsumer) {
        return BeanSupplier.of(ThrottledRequestExecutor.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> new Bucket4jThrottledRequestExecutor(bucket4jThrottledRequestConsumer))
            .otherwiseProxy()
            .get();
    }

    @ConditionalOnMissingBean(name = "bucket4jThrottledRequestStore")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public BucketStore bucket4jThrottledRequestStore(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(BucketStore.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val throttle = casProperties.getAuthn().getThrottle();
                return new InMemoryBucketStore(throttle.getBucket4j());
            })
            .otherwiseProxy()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "bucket4jThrottledRequestConsumer")
    public BucketConsumer bucket4jThrottledRequestConsumer(
        @Qualifier("bucket4jThrottledRequestStore")
        final BucketStore bucket4jThrottledRequestStore,
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(BucketConsumer.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val throttle = casProperties.getAuthn().getThrottle();
                return new DefaultBucketConsumer(bucket4jThrottledRequestStore, throttle.getBucket4j());
            })
            .otherwise(BucketConsumer::permitAll)
            .get();
    }
}
