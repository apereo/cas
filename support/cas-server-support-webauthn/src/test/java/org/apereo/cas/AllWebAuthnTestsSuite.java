package org.apereo.cas;

import org.apereo.cas.webauthn.storage.InMemoryWebAuthnCredentialRepositoryTests;
import org.apereo.cas.webauthn.storage.JsonResourceWebAuthnCredentialRepositoryTests;
import org.apereo.cas.webauthn.web.WebAuthnRegisteredDevicesEndpointTests;
import org.apereo.cas.webauthn.web.flow.*;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

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
    WebAuthnValidateSessionCredentialTokenActionTests.class,
    JsonResourceWebAuthnCredentialRepositoryTests.class,
    WebAuthnRegisteredDevicesEndpointTests.class,
    InMemoryWebAuthnCredentialRepositoryTests.class,
    WebAuthnAuthenticationWebflowEventResolverTests.class,
    WebAuthnAuthenticationWebflowActionTests.class,
    WebAuthnStartRegistrationActionTests.class,
    WebAuthnPreparePrimaryLoginActionTests.class
})
@Suite
public class AllWebAuthnTestsSuite {
}
