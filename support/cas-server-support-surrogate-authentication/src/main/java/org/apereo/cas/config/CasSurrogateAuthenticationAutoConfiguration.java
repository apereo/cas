package org.apereo.cas.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * This is {@link CasSurrogateAuthenticationAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@AutoConfiguration
@Import({
    SurrogateAuthenticationConfiguration.class,
    SurrogateAuthenticationAuditConfiguration.class,
    SurrogateAuthenticationMetadataConfiguration.class,
    SurrogateComponentSerializationConfiguration.class,
    SurrogateAuthenticationRestConfiguration.class
})
public class CasSurrogateAuthenticationAutoConfiguration {
}
