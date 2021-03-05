package org.apereo.cas;

import org.apereo.cas.webauthn.ActiveDirectoryWebAuthnCredentialRepositoryTests;
import org.apereo.cas.webauthn.OpenLdapWebAuthnCredentialRepositoryTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllLdapWebAuthnTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    OpenLdapWebAuthnCredentialRepositoryTests.class,
    ActiveDirectoryWebAuthnCredentialRepositoryTests.class
})
@RunWith(JUnitPlatform.class)
public class AllLdapWebAuthnTestsSuite {
}
