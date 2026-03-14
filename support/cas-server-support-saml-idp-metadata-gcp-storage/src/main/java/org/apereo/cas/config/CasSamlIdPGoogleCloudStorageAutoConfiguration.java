package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * This is {@link CasSamlIdPGoogleCloudStorageAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfiguration
@Import(SamlIdPGoogleCloudStorageIdPMetadataConfiguration.class)
public class CasSamlIdPGoogleCloudStorageAutoConfiguration {
}
