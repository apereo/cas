package org.apereo.cas.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.metrics.MetricsProperties;
import org.apereo.cas.influxdb.InfluxDbConnectionFactory;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.redis.core.RedisObjectFactory;
import org.influxdb.dto.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.ExportMetricWriter;
import org.springframework.boot.actuate.metrics.Metric;
import org.springframework.boot.actuate.metrics.opentsdb.OpenTsdbGaugeWriter;
import org.springframework.boot.actuate.metrics.repository.redis.RedisMetricRepository;
import org.springframework.boot.actuate.metrics.statsd.StatsdMetricWriter;
import org.springframework.boot.actuate.metrics.writer.GaugeWriter;
import org.springframework.boot.actuate.metrics.writer.MetricWriter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.TimeUnit;

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

    @ConditionalOnProperty(prefix = "cas.metrics.influxDb", name = "url")
    @Bean
    @ExportMetricWriter
    public GaugeWriter influxDbMetricsWriter() {
        final MetricsProperties.InfluxDb influxDb = casProperties.getMetrics().getInfluxDb();
        final InfluxDbConnectionFactory factory = new InfluxDbConnectionFactory(influxDb);
        return value -> {
            final Point point = Point.measurement(value.getName())
                    .time(value.getTimestamp().getTime(), TimeUnit.MILLISECONDS)
                    .addField("value", value.getValue())
                    .addField("name", value.getName())
                    .tag("type", value.getClass().getSimpleName())
                    .build();
            factory.write(point, influxDb.getDatabase());
        };
    }

    @ConditionalOnProperty(prefix = "cas.metrics.mongo", name = "collection")
    @Bean
    @ExportMetricWriter
    public GaugeWriter mongoDbMetricWriter() {
        final MetricsProperties.MongoDb prop = casProperties.getMetrics().getMongo();
        final MongoDbConnectionFactory factory = new MongoDbConnectionFactory();
        final MongoTemplate mongoTemplate = factory.buildMongoTemplate(prop);
        return metric -> {
            final MongoDbMetric metrics = new MongoDbMetric(metric);
            mongoTemplate.save(metrics, prop.getCollection());
        };
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
    private static class MongoDbMetric implements Serializable {
        private static final long serialVersionUID = 8587687286389110789L;

        private final String name;
        private final Number value;
        private final Date timestamp;

        MongoDbMetric(final Metric metric) {
            this.name = metric.getName();
            this.value = metric.getValue();
            this.timestamp = metric.getTimestamp();
        }

        public String getName() {
            return name;
        }

        public Number getValue() {
            return value;
        }

        public Date getTimestamp() {
            return timestamp;
        }
    }
}
