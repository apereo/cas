package org.apereo.cas.config;

import module java.base;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * This is {@link CasSurrogateAuthenticationWebflowAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@AutoConfiguration
@Import({
    SurrogateAuthenticationDelegationConfiguration.class,
    SurrogateAuthenticationPasswordlessConfiguration.class,
    SurrogateAuthenticationWebflowConfiguration.class
})
public class CasSurrogateAuthenticationWebflowAutoConfiguration {
}
