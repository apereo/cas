package org.apereo.cas;

import org.apereo.cas.webauthn.WebAuthnControllerTests;
import org.apereo.cas.webauthn.WebAuthnCredentialTests;
import org.apereo.cas.webauthn.WebAuthnMultifactorAuthenticationProviderTests;

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
    WebAuthnCredentialTests.class,
    WebAuthnControllerTests.class,
    WebAuthnMultifactorAuthenticationProviderTests.class
})
@RunWith(JUnitPlatform.class)
public class AllWebAuthnTestsSuite {
}
