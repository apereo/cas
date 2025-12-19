package org.apereo.cas.config;

import module java.base;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * This is {@link CasAmazonS3SamlMetadataAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@AutoConfiguration
@Import({
    SamlIdPAmazonS3RegisteredServiceMetadataConfiguration.class,
    AmazonS3SamlIdPMetadataConfiguration.class,
    AmazonS3SamlMetadataConfiguration.class
})
public class CasAmazonS3SamlMetadataAutoConfiguration {
}
