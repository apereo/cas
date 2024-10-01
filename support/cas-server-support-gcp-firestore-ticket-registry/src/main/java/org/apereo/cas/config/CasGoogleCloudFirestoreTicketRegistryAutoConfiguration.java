package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.catalog.CasTicketCatalogConfigurationValuesProvider;
import org.apereo.cas.ticket.registry.GoogleCloudFirestoreTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.CoreTicketUtils;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import com.google.cloud.firestore.Firestore;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import java.util.function.Function;

/**
 * This is {@link CasGoogleCloudFirestoreTicketRegistryAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = false)
@EnableScheduling
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.TicketRegistry, module = "gcp")
@AutoConfiguration
public class CasGoogleCloudFirestoreTicketRegistryAutoConfiguration {
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public TicketRegistry ticketRegistry(
        @Qualifier(TicketCatalog.BEAN_NAME)
        final TicketCatalog ticketCatalog,
        final CasConfigurationProperties casProperties,
        @Qualifier("firestore")
        final Firestore firestore,
        @Qualifier(TicketSerializationManager.BEAN_NAME)
        final TicketSerializationManager ticketSerializationManager,
        final ConfigurableApplicationContext applicationContext) {
        val firestoreProps = casProperties.getTicket().getRegistry().getGoogleCloudFirestore();
        val cipher = CoreTicketUtils.newTicketRegistryCipherExecutor(firestoreProps.getCrypto(), "firestore");
        return new GoogleCloudFirestoreTicketRegistry(cipher, ticketSerializationManager, ticketCatalog, applicationContext, firestore);
    }


    @Configuration(value = "GoogleCloudFirestoreTicketCatalogProviderConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class GoogleCloudFirestoreTicketCatalogProviderConfiguration {
        @ConditionalOnMissingBean(name = "googleCloudFirestoreTicketCatalogConfigurationValuesProvider")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasTicketCatalogConfigurationValuesProvider googleCloudFirestoreTicketCatalogConfigurationValuesProvider() {
            return new CasTicketCatalogConfigurationValuesProvider() {

                @Override
                public Function<CasConfigurationProperties, String> getServiceTicketStorageName() {
                    return p -> "serviceTicketsCollection";
                }

                @Override
                public Function<CasConfigurationProperties, String> getProxyTicketStorageName() {
                    return p -> "proxyTicketsCollection";
                }

                @Override
                public Function<CasConfigurationProperties, String> getTicketGrantingTicketStorageName() {
                    return p -> "ticketGrantingTicketsCollection";
                }

                @Override
                public Function<CasConfigurationProperties, String> getProxyGrantingTicketStorageName() {
                    return p -> "proxyGrantingTicketsCollection";
                }

                @Override
                public Function<CasConfigurationProperties, String> getTransientSessionStorageName() {
                    return p -> "transientSessionTicketsCollection";
                }
            };
        }
    }
}
