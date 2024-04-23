package org.apereo.cas.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * This is {@link CasDuoSecurityAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@AutoConfiguration
@Import({
    DuoSecurityComponentSerializationConfiguration.class,
    DuoSecurityMultifactorProviderBypassConfiguration.class,
    DuoSecurityAuthenticationEventExecutionPlanConfiguration.class,
    DuoSecurityConfiguration.class,
    DuoSecurityRestConfiguration.class,
    DuoSecurityPasswordlessAuthenticationConfiguration.class
})
public class CasDuoSecurityAutoConfiguration {
}
