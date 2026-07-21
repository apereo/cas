package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.scripting.ExecutableCompiledScript;
import org.apereo.cas.util.scripting.GroovyScriptCacheManagerEndpoint;
import org.apereo.cas.util.scripting.GroovyScriptResourceCacheManager;
import org.apereo.cas.util.scripting.ScriptResourceCacheManager;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.Ordered;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * This is {@link CasCoreScriptingAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@EnableScheduling
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Scripting)
@ImportAutoConfiguration(CasCoreUtilAutoConfiguration.class)
@AutoConfiguration
public class CasCoreScriptingAutoConfiguration {

    @Configuration(value = "CasCoreScriptingGroovyConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @Lazy(false)
    static class CasCoreScriptingGroovyConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = ScriptResourceCacheManager.BEAN_NAME)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ScriptResourceCacheManager<String, ExecutableCompiledScript> scriptResourceCacheManager(
            final CasConfigurationProperties casProperties) {
            return new GroovyScriptResourceCacheManager(casProperties.getCore().getGroovyCacheManager());
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnAvailableEndpoint
        public GroovyScriptCacheManagerEndpoint groovyScriptCacheManagerEndpoint(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(ScriptResourceCacheManager.BEAN_NAME)
            final ObjectProvider<ScriptResourceCacheManager<String, ExecutableCompiledScript>> scriptResourceCacheManager) {
            return new GroovyScriptCacheManagerEndpoint(casProperties, applicationContext, scriptResourceCacheManager);
        }
    }
    
}
