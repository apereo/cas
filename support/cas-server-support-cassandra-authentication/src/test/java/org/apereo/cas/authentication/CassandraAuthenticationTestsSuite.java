package org.apereo.cas.authentication;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link CassandraAuthenticationTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SelectClasses(
    CassandraAuthenticationHandlerTests.class
)
@RunWith(JUnitPlatform.class)
public class CassandraAuthenticationTestsSuite {
}
