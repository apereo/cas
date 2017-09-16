package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.metrics.MetricsProperties;
import org.apereo.cas.redis.core.RedisObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.ExportMetricWriter;
import org.springframework.boot.actuate.metrics.opentsdb.OpenTsdbGaugeWriter;
import org.springframework.boot.actuate.metrics.repository.redis.RedisMetricRepository;
import org.springframework.boot.actuate.metrics.statsd.StatsdMetricWriter;
import org.springframework.boot.actuate.metrics.writer.GaugeWriter;
import org.springframework.boot.actuate.metrics.writer.MetricWriter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * This is {@link CasMetricsRepositoryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("casMetricsRepositoryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasMetricsRepositoryConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnProperty(prefix = "cas.metrics.redis", name = "prefix")
    @Bean
    @ExportMetricWriter
    public MetricWriter redisMetricWriter() {
        final MetricsProperties.Redis redis = casProperties.getMetrics().getRedis();
        final RedisObjectFactory factory = new RedisObjectFactory();
        final RedisConnectionFactory connectionFactory = factory.newRedisConnectionFactory(redis);
        return new RedisMetricRepository(connectionFactory, redis.getPrefix(), redis.getKey());
    }

    @ConditionalOnProperty(prefix = "cas.metrics.openTsdb", name = "url")
    @Bean
    @ExportMetricWriter
    public GaugeWriter openTsdbMetricWriter() {
        final MetricsProperties.OpenTsdb prop = casProperties.getMetrics().getOpenTsdb();
        final OpenTsdbGaugeWriter w = new OpenTsdbGaugeWriter(prop.getConnectTimeout(), prop.getReadTimeout());
        w.setUrl(prop.getUrl());
        return w;
    }

    @ConditionalOnProperty(prefix = "cas.metrics.statsd", name = "host")
    @Bean
    @ExportMetricWriter
    public MetricWriter statsdMetricWriter() {
        final MetricsProperties.Statsd prop = casProperties.getMetrics().getStatsd();
        return new StatsdMetricWriter(prop.getPrefix(), prop.getHost(), prop.getPort());
    }
}
