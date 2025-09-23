package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * This is {@link CasSamlIdPDynamoDbAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfiguration
@Import({
    SamlIdPDynamoDbIdPMetadataConfiguration.class,
    SamlIdPDynamoDbRegisteredServiceMetadataConfiguration.class
})
public class CasSamlIdPDynamoDbAutoConfiguration {
}
