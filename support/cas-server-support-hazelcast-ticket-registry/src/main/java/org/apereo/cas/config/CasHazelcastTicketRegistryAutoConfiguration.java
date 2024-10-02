package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.hz.HazelcastConfigurationFactory;
import org.apereo.cas.hz.HazelcastMapCustomizer;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.catalog.CasTicketCatalogConfigurationValuesProvider;
import org.apereo.cas.ticket.registry.HazelcastTicketDocument;
import org.apereo.cas.ticket.registry.HazelcastTicketRegistry;
import org.apereo.cas.ticket.registry.MapAttributeValueExtractor;
import org.apereo.cas.ticket.registry.NoOpTicketRegistryCleaner;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistryCleaner;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.CoreTicketUtils;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import com.hazelcast.config.AttributeConfig;
import com.hazelcast.config.IndexConfig;
import com.hazelcast.config.IndexType;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.impl.HazelcastInstanceFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import java.util.ArrayList;

/**
 * Spring's Java configuration component for {@code HazelcastInstance} that is consumed and used by
 * {@link HazelcastTicketRegistry}.
 * <p>
 * This configuration class has the smarts to choose the configuration source for the {@link HazelcastInstance}
 * that it produces by either loading the native hazelcast XML config file from a resource location
 * or it creates the {@link HazelcastInstance} programmatically
 * with a handful properties and their defaults (if not set) that it exposes to CAS deployers.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 4.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.TicketRegistry, module = "hazelcast")
@AutoConfiguration
public class CasHazelcastTicketRegistryAutoConfiguration {

    @ConditionalOnMissingBean(name = "hazelcastTicketCatalogConfigurationValuesProvider")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasTicketCatalogConfigurationValuesProvider hazelcastTicketCatalogConfigurationValuesProvider() {
        return new CasTicketCatalogConfigurationValuesProvider() {
        };
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public TicketRegistry ticketRegistry(
        @Qualifier(TicketSerializationManager.BEAN_NAME) final TicketSerializationManager ticketSerializationManager,
        @Qualifier("casTicketRegistryHazelcastInstance") final HazelcastInstance casTicketRegistryHazelcastInstance,
        @Qualifier(TicketCatalog.BEAN_NAME) final TicketCatalog ticketCatalog,
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext) {
        val hz = casProperties.getTicket().getRegistry().getHazelcast();
        val cipher = CoreTicketUtils.newTicketRegistryCipherExecutor(hz.getCrypto(), "hazelcast");
        return new HazelcastTicketRegistry(cipher, ticketSerializationManager, ticketCatalog, applicationContext,
            casTicketRegistryHazelcastInstance, hz);
    }

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(name = "casTicketRegistryHazelcastInstance")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public HazelcastInstance casTicketRegistryHazelcastInstance(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(TicketCatalog.BEAN_NAME) final TicketCatalog ticketCatalog,
        final CasConfigurationProperties casProperties) {
        val hz = casProperties.getTicket().getRegistry().getHazelcast();
        LOGGER.debug("Creating Hazelcast instance for members [{}]", hz.getCluster().getNetwork().getMembers());
        val hazelcastInstance = HazelcastInstanceFactory.getOrCreateHazelcastInstance(HazelcastConfigurationFactory.build(hz));
        val ticketDefinitions = ticketCatalog.findAll();

        ticketDefinitions
            .stream()
            .map(defn -> {
                LOGGER.debug("Creating Hazelcast map configuration for [{}]", defn.getProperties());
                val props = defn.getProperties();
                val config = HazelcastConfigurationFactory.buildMapConfig(hz, props.getStorageName(), props.getStorageTimeout());
                if (config instanceof final MapConfig mapConfig) {
                    mapConfig.addIndexConfig(new IndexConfig(IndexType.HASH, "id"));
                    mapConfig.addIndexConfig(new IndexConfig(IndexType.HASH, "type"));
                    mapConfig.addIndexConfig(new IndexConfig(IndexType.HASH, "principal"));

                    val attributeConfig = new AttributeConfig();
                    attributeConfig.setName("attributes");
                    attributeConfig.setExtractorClassName(MapAttributeValueExtractor.class.getName());
                    mapConfig.addAttributeConfig(attributeConfig);
                }
                return config;
            })
            .peek(map -> {
                val customizers = new ArrayList<>(applicationContext.getBeansOfType(HazelcastMapCustomizer.class).values());
                AnnotationAwareOrderComparator.sortIfNecessary(customizers);
                customizers.forEach(customizer -> customizer.customize(map));
            })
            .forEach(map -> HazelcastConfigurationFactory.setConfigMap(map, hazelcastInstance.getConfig()));

        if (hz.getCore().isEnableJet()) {
            ticketDefinitions.forEach(defn -> {
                val query = buildCreateMappingQuery(defn);
                LOGGER.trace("Creating mapping for [{}] via [{}]", defn.getPrefix(), query);
                try (val __ = hazelcastInstance.getSql().execute(query)) {
                    LOGGER.info("Created Hazelcast SQL mapping for [{}]", defn.getPrefix());
                }
            });
        }
        return hazelcastInstance;
    }

    private static String buildCreateMappingQuery(final TicketDefinition defn) {
        val builder = new StringBuilder(String.format("CREATE MAPPING IF NOT EXISTS \"%s\" ", defn.getProperties().getStorageName()));
        builder.append("TYPE IMap ");
        builder.append("OPTIONS (");
        builder.append("'keyFormat' = 'java',");
        builder.append("'keyJavaClass' = 'java.lang.String',");
        builder.append("'valueFormat' = 'java',");
        builder.append(String.format("'valueJavaClass' = '%s'", HazelcastTicketDocument.class.getName()));
        builder.append(')');
        return builder.toString();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Lazy(false)
    public TicketRegistryCleaner ticketRegistryCleaner() {
        return NoOpTicketRegistryCleaner.getInstance();
    }
}
