package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.model.support.geode.GeodeProperties;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.catalog.CasTicketCatalogConfigurationValuesProvider;
import org.apereo.cas.ticket.registry.GeodeCache;
import org.apereo.cas.ticket.registry.GeodeTicketDocument;
import org.apereo.cas.ticket.registry.GeodeTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.CoreTicketUtils;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.ExpirationAction;
import org.apache.geode.cache.ExpirationAttributes;
import org.apache.geode.cache.RegionShortcut;
import org.apache.geode.pdx.ReflectionBasedAutoSerializer;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is {@link CasGeodeTicketRegistryAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.TicketRegistry, module = "geode")
@AutoConfiguration
@Slf4j
public class CasGeodeTicketRegistryAutoConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public TicketRegistry ticketRegistry(
        @Qualifier(TicketCatalog.BEAN_NAME)
        final TicketCatalog ticketCatalog,
        @Qualifier(TicketSerializationManager.BEAN_NAME)
        final TicketSerializationManager ticketSerializationManager,
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val geodeProperties = casProperties.getTicket().getRegistry().getGeode();
        val cipher = CoreTicketUtils.newTicketRegistryCipherExecutor(geodeProperties.getCrypto(), "geode");
        val geodeCaches = buildTicketCaches(geodeProperties, ticketCatalog);
        return new GeodeTicketRegistry(cipher, ticketSerializationManager,
            ticketCatalog, applicationContext, geodeCaches, geodeProperties);
    }

    @ConditionalOnMissingBean(name = "geodeTicketCatalogConfigurationValuesProvider")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasTicketCatalogConfigurationValuesProvider geodeTicketCatalogConfigurationValuesProvider() {
        return new CasTicketCatalogConfigurationValuesProvider() {
        };
    }

    private static Map<String, GeodeCache> buildTicketCaches(final GeodeProperties properties,
                                                             final TicketCatalog ticketCatalog) {
        val cacheFactory = new CacheFactory()
            .set("name", "CasGeodeCache")
            .set("mcast-port", String.valueOf(properties.getMulticastPort()))
            .set("log-level", LOGGER.isDebugEnabled() ? "DEBUG" : "WARN")
            .setPdxReadSerialized(true)
            .setPdxSerializer(new ReflectionBasedAutoSerializer(GeodeTicketDocument.class.getName()));
        
        if (StringUtils.isNotBlank(properties.getLocators()) && !Strings.CI.equals("none", properties.getLocators())) {
            cacheFactory.set("locators", properties.getLocators());
        }
        val cache = cacheFactory.create();
        val definitions = ticketCatalog.findAll();
        return definitions
            .stream()
            .map(Unchecked.function(definition -> {
                val storageName = definition.getProperties().getStorageName();
                val timeToLive = new ExpirationAttributes((int) definition.getProperties().getStorageTimeout(), ExpirationAction.DESTROY);
                val regionFactory = cache.<String, GeodeTicketDocument>createRegionFactory(RegionShortcut.REPLICATE);
                regionFactory.setStatisticsEnabled(true);
                regionFactory.setEntryTimeToLive(timeToLive);
                val region = regionFactory.create(storageName + "Region");
                
                val queryService = cache.getQueryService();
                val indexSource = "/%s t".formatted(region.getName());
                List.of("id", "kind", "principal", "prefix", "attributes").forEach(Unchecked.consumer(field ->
                    queryService.createIndex(field + "Index", "t." + field, indexSource)));
                return Pair.of(storageName, new GeodeCache(cache, region));
            }))
            .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }
}
