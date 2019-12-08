package org.apereo.cas.adaptors.trusted.web.flow;

import org.apereo.cas.AbstractCentralAuthenticationServiceTests;
import org.apereo.cas.adaptors.trusted.config.TrustedAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.web.flow.config.CasMultifactorAuthenticationWebflowConfiguration;

import org.springframework.context.annotation.Import;

/**
 * This is {@link BaseNonInteractiveCredentialsActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Import({
    CasMultifactorAuthenticationWebflowConfiguration.class,
    CasCoreMultifactorAuthenticationConfiguration.class,
    TrustedAuthenticationConfiguration.class
})
public abstract class BaseNonInteractiveCredentialsActionTests extends AbstractCentralAuthenticationServiceTests {
}
