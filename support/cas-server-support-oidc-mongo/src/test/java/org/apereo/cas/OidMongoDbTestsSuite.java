package org.apereo.cas;


import org.apereo.cas.config.OidcJwksMongoDbConfiguration;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link OidMongoDbTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SelectClasses(OidcJwksMongoDbConfiguration.class)
@Suite
public class OidMongoDbTestsSuite {
}
