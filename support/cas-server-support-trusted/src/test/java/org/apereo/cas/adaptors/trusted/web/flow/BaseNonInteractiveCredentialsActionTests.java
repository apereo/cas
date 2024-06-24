package org.apereo.cas.adaptors.trusted.web.flow;

import org.apereo.cas.AbstractCentralAuthenticationServiceTests;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasTrustedAuthenticationAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;

/**
 * This is {@link BaseNonInteractiveCredentialsActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@ImportAutoConfiguration({
    CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
    CasCoreMultifactorAuthenticationAutoConfiguration.class,
    CasTrustedAuthenticationAutoConfiguration.class
})
public abstract class BaseNonInteractiveCredentialsActionTests extends AbstractCentralAuthenticationServiceTests {
}
