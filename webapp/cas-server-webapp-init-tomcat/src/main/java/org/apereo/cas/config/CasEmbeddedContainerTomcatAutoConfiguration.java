package org.apereo.cas.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * This is {@link CasEmbeddedContainerTomcatAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@AutoConfiguration(before = ServletWebServerFactoryAutoConfiguration.class)
@Import({CasEmbeddedContainerTomcatConfiguration.class, CasEmbeddedContainerTomcatFiltersConfiguration.class})
public class CasEmbeddedContainerTomcatAutoConfiguration {
}
