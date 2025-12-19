package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.ServerConnector;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jetty.JettyServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/**
 * This is {@link CasEmbeddedContainerJettyAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Jetty)
public class CasEmbeddedContainerJettyAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = "casJettyServerCustomizer")
    public JettyServerCustomizer casJettyServerCustomizer(final CasConfigurationProperties casProperties) {
        return server -> {
            for (val connector : server.getConnectors()) {
                if (connector instanceof final ServerConnector serverConnector) {
                    val connectionFactory = serverConnector.getConnectionFactory(HttpConnectionFactory.class);
                    if (connectionFactory != null) {
                        var secureRequestCustomizer = connectionFactory.getHttpConfiguration().getCustomizer(SecureRequestCustomizer.class);
                        if (secureRequestCustomizer == null) {
                            secureRequestCustomizer = new SecureRequestCustomizer();
                            connectionFactory.getHttpConfiguration().addCustomizer(secureRequestCustomizer);
                        }
                        secureRequestCustomizer.setSniHostCheck(casProperties.getServer().getJetty().isSniHostCheck());
                    }
                }
            }
        };
    }
}
