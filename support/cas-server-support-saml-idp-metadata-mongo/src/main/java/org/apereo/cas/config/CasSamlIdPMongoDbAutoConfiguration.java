package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * This is {@link CasSamlIdPMongoDbAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfiguration
@Import({
    SamlIdPMongoDbIdPMetadataConfiguration.class,
    SamlIdPMongoDbRegisteredServiceMetadataConfiguration.class
})
public class CasSamlIdPMongoDbAutoConfiguration {
}
