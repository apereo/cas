package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import io.etcd.jetcd.Client;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.StringUtils;

/**
 * This is {@link CasEtcdCloudConfigBootstrapAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.CasConfiguration, module = "etcd")
@AutoConfiguration
public class CasEtcdCloudConfigBootstrapAutoConfiguration {
    /**
     * The CAS etc configuration key prefix.
     */
    public static final String CAS_CONFIGURATION_PREFIX = "cas.spring.cloud.etcd";

    @Bean
    @ConditionalOnMissingBean(name = "etcdPropertySourceLocator")
    public PropertySourceLocator etcdPropertySourceLocator(
        @Qualifier("etcdClient") final Client etcdClient) {
        return new EtcdPropertySourceLocator(etcdClient);
    }

    @Bean
    @ConditionalOnMissingBean(name = "etcdClient")
    public Client etcdClient(final ConfigurableEnvironment environment) {
        val endpoints = environment.getRequiredProperty(CAS_CONFIGURATION_PREFIX + ".endpoints");
        return Client.builder()
            .endpoints(StringUtils.commaDelimitedListToSet(endpoints).toArray(String[]::new))
            .executorService(Executors.newVirtualThreadPerTaskExecutor())
            .build();
    }
}
