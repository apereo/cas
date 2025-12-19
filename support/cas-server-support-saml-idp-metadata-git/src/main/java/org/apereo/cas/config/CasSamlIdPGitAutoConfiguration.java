package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * This is {@link CasSamlIdPGitAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Import({
    SamlIdPGitRegisteredServiceMetadataConfiguration.class,
    SamlIdPGitIdPMetadataConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfiguration
public class CasSamlIdPGitAutoConfiguration {
}
