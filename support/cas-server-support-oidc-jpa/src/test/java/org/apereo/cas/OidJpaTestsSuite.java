package org.apereo.cas;

import org.apereo.cas.config.OidcJwksJpaConfiguration;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link OidJpaTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SelectClasses(OidcJwksJpaConfiguration.class)
@Suite
public class OidJpaTestsSuite {
}
