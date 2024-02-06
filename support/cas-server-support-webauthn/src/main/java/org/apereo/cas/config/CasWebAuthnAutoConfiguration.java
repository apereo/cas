package org.apereo.cas.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * This is {@link CasWebAuthnAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@AutoConfiguration
@Import({
    WebAuthnComponentSerializationConfiguration.class,
    WebAuthnConfiguration.class,
    WebAuthnWebflowConfiguration.class,
    WebAuthnMultifactorProviderBypassConfiguration.class
})
public class CasWebAuthnAutoConfiguration {
}
