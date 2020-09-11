package org.apereo.cas;

import org.apereo.cas.webauthn.JpaWebAuthnCredentialRepositoryTests;
import org.apereo.cas.webauthn.MariaDbJpaWebAuthnCredentialRepositoryTests;
import org.apereo.cas.webauthn.MySQLJpaWebAuthnCredentialRepositoryTests;
import org.apereo.cas.webauthn.PostgresJpaWebAuthnCredentialRepositoryTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllJpaWebAuthnTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    JpaWebAuthnCredentialRepositoryTests.class,
    PostgresJpaWebAuthnCredentialRepositoryTests.class,
    MariaDbJpaWebAuthnCredentialRepositoryTests.class,
    MySQLJpaWebAuthnCredentialRepositoryTests.class
})
@RunWith(JUnitPlatform.class)
public class AllJpaWebAuthnTestsSuite {
}
