package org.apereo.cas;


import org.apereo.cas.oidc.jwks.generator.OidcMongoDbJsonWebKeystoreGeneratorServiceTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link OidMongoDbTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SelectClasses(OidcMongoDbJsonWebKeystoreGeneratorServiceTests.class)
@Suite
public class OidMongoDbTestsSuite {
}
