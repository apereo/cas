package org.apereo.cas.adaptors.trusted.web.flow;

import org.apereo.cas.AbstractCentralAuthenticationServiceTests;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.TrustedAuthenticationAutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * This is {@link BaseNonInteractiveCredentialsActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Import({
    CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
    CasCoreMultifactorAuthenticationAutoConfiguration.class,
    TrustedAuthenticationAutoConfiguration.class
})
public abstract class BaseNonInteractiveCredentialsActionTests extends AbstractCentralAuthenticationServiceTests {
}
