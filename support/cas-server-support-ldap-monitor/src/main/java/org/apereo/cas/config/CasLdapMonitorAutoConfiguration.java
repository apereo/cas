package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.model.core.monitor.LdapMonitorProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.monitor.PooledLdapConnectionFactoryHealthIndicator;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.lambda.Unchecked;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.SearchConnectionValidator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ListFactoryBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.health.autoconfigure.contributor.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.health.contributor.CompositeHealthContributor;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasLdapMonitorAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Monitoring, module = "ldap")
@AutoConfiguration
public class CasLdapMonitorAutoConfiguration {
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public ListFactoryBean pooledLdapConnectionFactoryHealthIndicatorListFactoryBean() {
        val list = new ListFactoryBean() {
            @Override
            protected void destroyInstance(final List list) {
                Objects.requireNonNull(list).forEach(connectionFactory ->
                    ((ConnectionFactory) connectionFactory).close()
                );
            }
        };
        list.setSourceList(new ArrayList<>());
        return list;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnEnabledHealthIndicator("pooledLdapConnectionFactoryHealthIndicator")
    public CompositeHealthContributor pooledLdapConnectionFactoryHealthIndicator(
        final CasConfigurationProperties casProperties,
        @Qualifier("pooledLdapConnectionFactoryHealthIndicatorListFactoryBean")
        final ListFactoryBean factoryBean) throws Exception {
        val ldaps = casProperties.getMonitor().getLdap();
        val connectionFactoryList = Objects.requireNonNull(factoryBean.getObject());
        val contributors = new LinkedHashMap<>();
        ldaps.stream()
            .filter(LdapMonitorProperties::isEnabled)
            .map(Unchecked.function(ldap -> {
                val executor = Beans.newThreadPoolExecutorFactoryBean(ldap.getPool());
                val connectionFactory = LdapUtils.newLdaptivePooledConnectionFactory(ldap);
                connectionFactoryList.add(connectionFactory);
                val healthIndicator = new PooledLdapConnectionFactoryHealthIndicator(Beans.newDuration(ldap.getMaxWait()).toMillis(),
                    connectionFactory, executor.getObject(), new SearchConnectionValidator());
                val name = StringUtils.defaultIfBlank(ldap.getName(), UUID.randomUUID().toString());
                return Pair.of(name, healthIndicator);
            }))
            .forEach(it -> contributors.put(it.getKey(), it.getValue()));
        return CompositeHealthContributor.fromMap((Map) contributors);
    }
}
