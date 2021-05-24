package org.apereo.cas.authentication;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link CassandraAuthenticationTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SelectClasses(
    CassandraAuthenticationHandlerTests.class
)
@Suite
public class CassandraAuthenticationTestsSuite {
}
