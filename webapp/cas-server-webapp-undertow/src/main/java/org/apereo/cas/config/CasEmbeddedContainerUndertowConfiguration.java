package org.apereo.cas.config;

import org.apereo.cas.CasEmbeddedContainerUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.EmbeddedServletContainerAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * This is {@link CasEmbeddedContainerUndertowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("casEmbeddedContainerUndertowConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(name = CasEmbeddedContainerUtils.EMBEDDED_CONTAINER_CONFIG_ACTIVE, havingValue = "true")
@AutoConfigureBefore(EmbeddedServletContainerAutoConfiguration.class)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
public class CasEmbeddedContainerUndertowConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasEmbeddedContainerUndertowConfiguration.class);
    
    @Autowired
    private CasConfigurationProperties casProperties;
}
