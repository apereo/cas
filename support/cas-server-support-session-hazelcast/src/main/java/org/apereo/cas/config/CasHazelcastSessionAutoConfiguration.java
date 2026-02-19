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
        val duration = ObjectUtils.getIfNull(sessionProperties.getTimeout(),
            serverProperties.getServlet().getSession().getTimeout());

        HazelcastSessionConfiguration.applySerializationConfig(config);
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
        return hazelcastInstance;
    }
}
