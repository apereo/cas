package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.hz.HazelcastConfigurationFactory;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import com.hazelcast.config.IndexConfig;
import com.hazelcast.config.IndexType;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.impl.HazelcastInstanceFactory;
import com.hazelcast.nio.serialization.compact.CompactReader;
import com.hazelcast.nio.serialization.compact.CompactSerializer;
import com.hazelcast.nio.serialization.compact.CompactWriter;
import com.hazelcast.spring.session.HazelcastIndexedSessionRepository;
import com.hazelcast.spring.session.HazelcastSessionConfiguration;
import com.hazelcast.spring.session.config.annotation.SpringSessionHazelcastInstance;
import com.hazelcast.spring.session.config.annotation.web.http.EnableHazelcastHttpSession;
import lombok.val;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.session.autoconfigure.SessionProperties;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasHazelcastSessionAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableHazelcastHttpSession(disableSessionMapAutoconfiguration = true)
@EnableConfigurationProperties({CasConfigurationProperties.class, SessionProperties.class, ServerProperties.class})
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SessionManagement, module = "hazelcast")
@AutoConfiguration
public class CasHazelcastSessionAutoConfiguration {

    @Bean(destroyMethod = "shutdown")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @SpringSessionHazelcastInstance
    public HazelcastInstance hazelcastInstance(final CasConfigurationProperties casProperties,
                                               final SessionProperties sessionProperties,
                                               final ServerProperties serverProperties) {
        val hz = casProperties.getWebflow().getSession().getServer().getHazelcast();
        val config = HazelcastConfigurationFactory.build(hz);
        config.getSerializationConfig().getCompactSerializationConfig()
            .addSerializer(new InstantCompactSerializer())
            .addSerializer(new DurationCompactSerializer());

        val duration = ObjectUtils.getIfNull(sessionProperties.getTimeout(),
            serverProperties.getServlet().getSession().getTimeout());

        val hazelcastInstance = HazelcastInstanceFactory.getOrCreateHazelcastInstance(config);
        val mapConfig = HazelcastConfigurationFactory.buildMapConfig(hz,
            HazelcastIndexedSessionRepository.DEFAULT_SESSION_MAP_NAME, duration.toSeconds());
        if (mapConfig instanceof final MapConfig finalConfig) {
            finalConfig.addIndexConfig(new IndexConfig(
                IndexType.HASH,
                HazelcastIndexedSessionRepository.PRINCIPAL_NAME_ATTRIBUTE
            ));
        }
        HazelcastConfigurationFactory.setConfigMap(mapConfig, hazelcastInstance.getConfig());
        HazelcastSessionConfiguration.applySerializationConfig(config);

        return hazelcastInstance;
    }

    private static final class InstantCompactSerializer implements CompactSerializer<Instant> {
        @Override
        public Instant read(final CompactReader reader) {
            val epochSecond = reader.readInt64("epochSecond");
            val nano = reader.readInt32("nano");
            return Instant.ofEpochSecond(epochSecond, nano);
        }

        @Override
        public void write(final CompactWriter writer, final Instant instant) {
            writer.writeInt64("epochSecond", instant.getEpochSecond());
            writer.writeInt32("nano", instant.getNano());
        }

        @Override
        public String getTypeName() {
            return "java.time.Instant";
        }

        @Override
        public Class<Instant> getCompactClass() {
            return Instant.class;
        }
    }

    private static final class DurationCompactSerializer implements CompactSerializer<Duration> {

        @Override
        public Duration read(final CompactReader reader) {
            val seconds = reader.readInt64("seconds");
            val nanos = reader.readInt32("nanos");
            return Duration.ofSeconds(seconds, nanos);
        }

        @Override
        public void write(final CompactWriter writer, final Duration duration) {
            writer.writeInt64("seconds", duration.getSeconds());
            writer.writeInt32("nanos", duration.getNano());
        }

        @Override
        public String getTypeName() {
            return "java.time.Duration";
        }

        @Override
        public Class<Duration> getCompactClass() {
            return Duration.class;
        }
    }
}
