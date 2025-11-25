package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.catalog.CasTicketCatalogConfigurationValuesProvider;
import org.apereo.cas.ticket.registry.IgniteTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.CoreTicketUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteServer;
import org.apache.ignite.InitParameters;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

/**
 * This is {@link CasIgniteTicketRegistryAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.TicketRegistry, module = "ignite")
@AutoConfiguration
@Slf4j
public class CasIgniteTicketRegistryAutoConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public TicketRegistry ticketRegistry(
        @Qualifier(TicketCatalog.BEAN_NAME)
        final TicketCatalog ticketCatalog,
        @Qualifier(TicketSerializationManager.BEAN_NAME)
        final TicketSerializationManager ticketSerializationManager,
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) throws Exception {
        val igniteProperties = casProperties.getTicket().getRegistry().getIgnite();
        val cipher = CoreTicketUtils.newTicketRegistryCipherExecutor(igniteProperties.getCrypto(), "ignite");
        val configContent = String.format(
            """
                    ignite {
                        network {
                            port: %d,
                            nodeFinder { netClusterNodes: [ "%s" ] }
                        }
                    }
                """,
            igniteProperties.getPort(),
            String.join(",", igniteProperties.getIgniteServers())
        ).stripIndent().trim();

        val workDir = new File(FileUtils.getTempDirectory(), "ignite-work-" + igniteProperties.getNodeName()).toPath();
        Files.createDirectories(workDir);
        val configPath = workDir.resolve("ignite-config.conf");
        LOGGER.debug("Creating Ignite configuration at [{}] with content: [{}]", configPath, configContent);
        Files.writeString(configPath, configContent, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        val serverFuture = IgniteServer.startAsync(igniteProperties.getNodeName(), configPath, workDir);
        val igniteServer = serverFuture.join();

        if (igniteProperties.isInitializeCluster()) {
            FunctionUtils.doAndHandle(_ -> {
                val initParams = InitParameters.builder()
                    .metaStorageNodeNames(igniteProperties.getNodeName())
                    .clusterName(igniteProperties.getClusterName())
                    .build();
                igniteServer.initCluster(initParams);
            });
        }
        val ignite = igniteServer.api();

        val definitions = ticketCatalog.findAll();
        definitions.forEach(definition -> {
            val fields = new ArrayList<String>();
            fields.add("id VARCHAR PRIMARY KEY");
            fields.add("type VARCHAR");
            fields.add("principal VARCHAR");
            fields.add("ticket VARBINARY");
            fields.add("expire_at TIMESTAMP");
            fields.add("prefix VARCHAR");
            fields.add("attributes VARCHAR");
            val tableName = definition.getProperties().getStorageName();
            val query = "CREATE TABLE IF NOT EXISTS %s (%s)".formatted(tableName, String.join(",", fields));
            try (val _ = ignite.sql().execute(null, query)) {
                LOGGER.info("Created Ignite table [{}] for ticket definition [{}]", tableName, definition.getPrefix());
            }
            createIndexForField("id", definition, ignite, tableName);
            createIndexForField("principal", definition, ignite, tableName);
            createIndexForField("type", definition, ignite, tableName);
            createIndexForField("prefix", definition, ignite, tableName);
        });

        return new IgniteTicketRegistry(cipher, ticketSerializationManager,
            ticketCatalog, applicationContext, ignite, igniteServer, igniteProperties);
    }

    private static void createIndexForField(final String field, final TicketDefinition definition, final Ignite ignite, final String tableName) {
        try (val _ = ignite.sql().execute(null, "CREATE INDEX IF NOT EXISTS idx_%s ON %s (%s)".formatted(field, tableName, field))) {
            LOGGER.info("Created index on table [{}] for ticket definition [{}]", tableName, definition.getPrefix());
        }
    }

    @ConditionalOnMissingBean(name = "igniteTicketCatalogConfigurationValuesProvider")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasTicketCatalogConfigurationValuesProvider igniteTicketCatalogConfigurationValuesProvider() {
        return new CasTicketCatalogConfigurationValuesProvider() {
        };
    }
}
