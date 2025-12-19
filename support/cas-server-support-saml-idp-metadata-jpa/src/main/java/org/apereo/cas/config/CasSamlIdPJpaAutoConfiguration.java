package org.apereo.cas.config;

import module java.base;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * This is {@link CasSamlIdPJpaAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@AutoConfiguration
@Import({
    SamlIdPJpaRegisteredServiceMetadataConfiguration.class,
    SamlIdPJpaIdPMetadataConfiguration.class
})
public class CasSamlIdPJpaAutoConfiguration {
}
