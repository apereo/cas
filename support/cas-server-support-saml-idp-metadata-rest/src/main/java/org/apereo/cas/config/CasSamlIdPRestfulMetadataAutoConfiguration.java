package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * This is {@link CasSamlIdPRestfulMetadataAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfiguration
@Import({
    SamlIdPRestfulMetadataConfiguration.class,
    SamlIdPRestfulIdPMetadataConfiguration.class
})
public class CasSamlIdPRestfulMetadataAutoConfiguration {
}
