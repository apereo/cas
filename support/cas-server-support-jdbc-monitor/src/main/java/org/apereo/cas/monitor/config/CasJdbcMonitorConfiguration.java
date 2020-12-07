package org.apereo.cas.monitor.config;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.monitor.JdbcDataSourceHealthIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.CompositeHealthContributor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;


/**
 * This is {@link CasJdbcMonitorConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casJdbcMonitorConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasJdbcMonitorConfiguration {
    private static final int MAP_SIZE = 8;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Bean
    @RefreshScope
    @ConditionalOnEnabledHealthIndicator("dataSourceHealthIndicator")
    public HealthIndicator dataSourceHealthIndicator() {
        val jdbcs = casProperties.getMonitor().getJdbc();
        val contributors = new LinkedHashMap<>(MAP_SIZE);
        jdbcs.stream()
            .filter(jdbc -> jdbc.getValidationQuery() != null)
            .map(jdbc -> {
                val executor = Beans.newThreadPoolExecutorFactoryBean(jdbc.getPool());
                executor.afterPropertiesSet();
                val healthIndicator = new JdbcDataSourceHealthIndicator(Beans.newDuration(jdbc.getMaxWait()).toMillis(),
                        JpaBeans.newDataSource(jdbc), executor.getObject(), jdbc.getValidationQuery());

                val name = StringUtils.defaultIfBlank(jdbc.getUrl(), UUID.randomUUID().toString());
                return Pair.of(name, healthIndicator);
            })
            .forEach(it -> contributors.put(it.getKey(), it.getValue()));    
        return CompositeHealthContributor.fromMap((Map) contributors);    
    }
}
