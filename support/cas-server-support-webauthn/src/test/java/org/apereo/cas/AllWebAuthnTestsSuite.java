package org.apereo.cas;

import org.apereo.cas.webauthn.storage.JsonResourceWebAuthnCredentialRepositoryTests;
import org.apereo.cas.webauthn.web.WebAuthnRegisteredDevicesEndpointTests;
import org.apereo.cas.webauthn.web.flow.WebAuthnAccountCheckRegistrationActionTests;
import org.apereo.cas.webauthn.web.flow.WebAuthnAccountSaveRegistrationActionTests;
import org.apereo.cas.webauthn.web.flow.WebAuthnAuthenticationWebflowActionTests;
import org.apereo.cas.webauthn.web.flow.WebAuthnAuthenticationWebflowEventResolverTests;
import org.apereo.cas.webauthn.web.flow.WebAuthnMultifactorWebflowConfigurerTests;
import org.apereo.cas.webauthn.web.flow.WebAuthnStartAuthenticationActionTests;
import org.apereo.cas.webauthn.web.flow.WebAuthnStartRegistrationActionTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllWebAuthnTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    WebAuthnMultifactorWebflowConfigurerTests.class,
    WebAuthnAccountCheckRegistrationActionTests.class,
    WebAuthnAccountSaveRegistrationActionTests.class,
    WebAuthnStartAuthenticationActionTests.class,
    JsonResourceWebAuthnCredentialRepositoryTests.class,
    WebAuthnRegisteredDevicesEndpointTests.class,
    WebAuthnAuthenticationWebflowEventResolverTests.class,
    WebAuthnAuthenticationWebflowActionTests.class,
    WebAuthnStartRegistrationActionTests.class
})
@RunWith(JUnitPlatform.class)
public class AllWebAuthnTestsSuite {
}
