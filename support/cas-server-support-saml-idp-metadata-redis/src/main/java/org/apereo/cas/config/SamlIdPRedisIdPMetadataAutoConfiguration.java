package org.apereo.cas.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * This is {@link SamlIdPRedisIdPMetadataAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@AutoConfiguration
@Import({
    SamlIdPRedisRegisteredServiceMetadataConfiguration.class,
    SamlIdPRedisIdPMetadataConfiguration.class
})
public class SamlIdPRedisIdPMetadataAutoConfiguration {
}
