package org.apereo.cas.adaptors.trusted.web.flow;

import org.apereo.cas.AbstractCentralAuthenticationServiceTests;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.CasMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.TrustedAuthenticationConfiguration;

import org.springframework.context.annotation.Import;

/**
 * This is {@link BaseNonInteractiveCredentialsActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Import({
    CasMultifactorAuthenticationWebflowAutoConfiguration.class,
    CasCoreMultifactorAuthenticationConfiguration.class,
    TrustedAuthenticationConfiguration.class
})
public abstract class BaseNonInteractiveCredentialsActionTests extends AbstractCentralAuthenticationServiceTests {
}
