package org.apereo.cas.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * This is {@link SurrogateAuthenticationWebflowAutoConfiguration}.
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
public class SurrogateAuthenticationWebflowAutoConfiguration {
}
