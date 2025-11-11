package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.hz.HazelcastConfigurationFactory;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import com.hazelcast.config.AttributeConfig;
import com.hazelcast.config.IndexConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.impl.HazelcastInstanceFactory;
import com.hazelcast.spring.session.HazelcastIndexedSessionRepository;
import com.hazelcast.spring.session.HazelcastSessionSerializer;
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
import org.springframework.session.MapSession;

/**
 * This is {@link CasHazelcastSessionAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableHazelcastHttpSession
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
        val serializerConfig = new SerializerConfig();
        serializerConfig.setImplementation(new HazelcastSessionSerializer()).setTypeClass(MapSession.class);
        config.getSerializationConfig().addSerializerConfig(serializerConfig);

        val duration = ObjectUtils.getIfNull(sessionProperties.getTimeout(),
            serverProperties.getServlet().getSession().getTimeout());

        val hazelcastInstance = HazelcastInstanceFactory.getOrCreateHazelcastInstance(config);
        val mapConfig = HazelcastConfigurationFactory.buildMapConfig(hz,
            HazelcastIndexedSessionRepository.DEFAULT_SESSION_MAP_NAME, duration.toSeconds());
        if (mapConfig instanceof final MapConfig finalConfig) {
            val attributeConfig = new AttributeConfig();
            attributeConfig.setName(HazelcastIndexedSessionRepository.PRINCIPAL_NAME_ATTRIBUTE);
            attributeConfig.setExtractorClassName(HazelcastSessionPrincipalNameExtractor.class.getName());
            finalConfig.addAttributeConfig(attributeConfig);
            val indexConfig = new IndexConfig();
            indexConfig.addAttribute(HazelcastIndexedSessionRepository.PRINCIPAL_NAME_ATTRIBUTE);
            finalConfig.addIndexConfig(indexConfig);
        }
        HazelcastConfigurationFactory.setConfigMap(mapConfig, hazelcastInstance.getConfig());
        return hazelcastInstance;
    }
}
