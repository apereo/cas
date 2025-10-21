package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.tomcat.CasTomcatServletWebServerFactory;
import org.apereo.cas.tomcat.CasTomcatServletWebServerFactoryCustomizer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apache.catalina.startup.Tomcat;
import org.apache.coyote.http2.Http2Protocol;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.tomcat.autoconfigure.TomcatServerProperties;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;
import org.springframework.boot.web.server.servlet.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * This is {@link CasEmbeddedContainerTomcatConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnClass({Tomcat.class, Http2Protocol.class})
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.ApacheTomcat)
@Configuration(value = "CasEmbeddedContainerTomcatConfiguration", proxyBeanMethods = false)
class CasEmbeddedContainerTomcatConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "casServletWebServerFactory")
    public ServletWebServerFactory casServletWebServerFactory(
        final ServerProperties serverProperties,
        final CasConfigurationProperties casProperties) {
        return new CasTomcatServletWebServerFactory(casProperties, serverProperties);
    }

    @ConditionalOnMissingBean(name = "casTomcatEmbeddedServletContainerCustomizer")
    @Bean
    public WebServerFactoryCustomizer casTomcatEmbeddedServletContainerCustomizer(
        final ServerProperties serverProperties,
        final TomcatServerProperties tomcatServerProperties,
        final CasConfigurationProperties casProperties) {
        return new CasTomcatServletWebServerFactoryCustomizer(serverProperties, tomcatServerProperties, casProperties);
    }
}
