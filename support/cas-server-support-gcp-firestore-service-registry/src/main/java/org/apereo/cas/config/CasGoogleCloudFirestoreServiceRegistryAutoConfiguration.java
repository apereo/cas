package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.GoogleCloudFirestoreServiceRegistry;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServiceRegistryListener;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import com.google.cloud.firestore.Firestore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.scheduling.annotation.EnableScheduling;
import java.util.List;

/**
 * This is {@link CasGoogleCloudFirestoreServiceRegistryAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableScheduling
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.ServiceRegistry, module = "gcp-firestore")
@AutoConfiguration
public class CasGoogleCloudFirestoreServiceRegistryAutoConfiguration {
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "gcpFirestoreServiceRegistry")
    public ServiceRegistry gcpFirestoreServiceRegistry(
        final CasConfigurationProperties casProperties,
        @Qualifier("firestore")
        final Firestore firestore,
        final ObjectProvider<List<ServiceRegistryListener>> serviceRegistryListeners,
        final ConfigurableApplicationContext applicationContext) {
        return new GoogleCloudFirestoreServiceRegistry(applicationContext, serviceRegistryListeners.getObject(),
            firestore, casProperties.getServiceRegistry().getGoogleCloudFirestore().getCollection());
    }

    @Bean
    @ConditionalOnMissingBean(name = "gcpFirestoreServiceRegistryExecutionPlanConfigurer")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public ServiceRegistryExecutionPlanConfigurer gcpFirestoreServiceRegistryExecutionPlanConfigurer(
        @Qualifier("gcpFirestoreServiceRegistry")
        final ServiceRegistry gitServiceRegistry) {
        return plan -> plan.registerServiceRegistry(gitServiceRegistry);
    }
}
