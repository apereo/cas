package org.apereo.cas;

import org.apereo.cas.oidc.jwks.generator.OidcJpaJsonWebKeystoreGeneratorServiceTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link OidJpaTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SelectClasses(OidcJpaJsonWebKeystoreGeneratorServiceTests.class)
@Suite
public class OidJpaTestsSuite {
}
